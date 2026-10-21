
import org.json.simple.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.avro.Conversions;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import com.microsoft.sqlserver.jdbc.ISQLServerBulkData;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopyOptions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Table {

    // Paramètre "updateExisting" du fichier de config : {"F_ARTICLE":"0", ...}
    // Une table absente (ou valeur différente de "0") => les valeurs existantes sont mises à jour.
    private static JSONObject updateExistingConfig = new JSONObject();

    public static void setUpdateExistingConfig(Object config) {
        updateExistingConfig = (config instanceof JSONObject) ? (JSONObject) config : new JSONObject();
    }

    public static boolean isUpdateEnabled(String tableName) {
        Object value = updateExistingConfig.get(tableName);
        return value == null || !"0".equals(value.toString());
    }

    /**
     * T-SQL qui, avant une insertion en bloc, écarte de la table temporaire les lignes qui violeraient
     * une clé étrangère de la table cible. Les contraintes sont lues dans sys.foreign_keys au moment
     * de l'exécution ; chaque ligne écartée est tracée dans config.DB_Errors.
     *
     * @param rowLabel expression SQL (alias t) décrivant la ligne dans le journal
     */
    public static String foreignKeyCheck(String targetTable, String tempTable, String rowLabel, String filename) {
        return """
                DECLARE @fkTarget SYSNAME = N'%s', @fkTemp SYSNAME = N'%s', @fkFile NVARCHAR(400) = N'%s';
                DECLARE @fkLabel NVARCHAR(MAX) = N'%s';
                DECLARE @fkId INT, @fkName SYSNAME, @fkRef NVARCHAR(400), @fkCond NVARCHAR(MAX), @fkNotNull NVARCHAR(MAX),
                        @fkCols NVARCHAR(MAX), @fkMissing INT, @fkSql NVARCHAR(MAX);
                DECLARE fkCur CURSOR LOCAL FAST_FORWARD FOR
                    SELECT fk.object_id, fk.name,
                           QUOTENAME(OBJECT_SCHEMA_NAME(fk.referenced_object_id)) + N'.' + QUOTENAME(OBJECT_NAME(fk.referenced_object_id))
                    FROM sys.foreign_keys fk
                    WHERE fk.parent_object_id = OBJECT_ID(@fkTarget) AND fk.is_disabled = 0
                      AND fk.referenced_object_id <> fk.parent_object_id;
                OPEN fkCur;
                FETCH NEXT FROM fkCur INTO @fkId, @fkName, @fkRef;
                WHILE @@FETCH_STATUS = 0
                BEGIN
                    SELECT @fkCond = N'', @fkNotNull = N'', @fkCols = N'', @fkMissing = 0;
                    SELECT @fkCond = @fkCond + N' AND r.' + QUOTENAME(rc.name) + N' = t.' + QUOTENAME(pc.name),
                           @fkNotNull = @fkNotNull + N' AND t.' + QUOTENAME(pc.name) + N' IS NOT NULL',
                           @fkCols = @fkCols + CASE WHEN @fkCols = N'' THEN N'' ELSE N', ' END + pc.name,
                           @fkMissing = @fkMissing + CASE WHEN tc.name IS NULL THEN 1 ELSE 0 END
                    FROM sys.foreign_key_columns fkc
                    INNER JOIN sys.columns pc ON pc.object_id = fkc.parent_object_id AND pc.column_id = fkc.parent_column_id
                    INNER JOIN sys.columns rc ON rc.object_id = fkc.referenced_object_id AND rc.column_id = fkc.referenced_column_id
                    LEFT JOIN tempdb.sys.columns tc ON tc.object_id = OBJECT_ID(N'tempdb..' + @fkTemp)
                                                   AND tc.name COLLATE DATABASE_DEFAULT = pc.name COLLATE DATABASE_DEFAULT
                    WHERE fkc.constraint_object_id = @fkId;

                    -- contrôle seulement si toutes les colonnes de la clé sont présentes dans la table temporaire
                    IF @fkMissing = 0
                    BEGIN
                        SET @fkSql = N'INSERT INTO config.DB_Errors SELECT SUSER_SNAME(), 547, NULL, NULL, NULL, NULL, N'''
                            + REPLACE(N'Clé étrangère ' + @fkName + N' : ' + @fkCols + N' introuvable dans ' + @fkRef + N', ligne non reprise', N'''', N'''''')
                            + N''', ' + @fkLabel + N' + N'' fileName : ' + REPLACE(@fkFile, N'''', N'''''') + N''', N''' + @fkTarget + N''', GETDATE()'
                            + N' FROM ' + @fkTemp + N' t WHERE 1 = 1' + @fkNotNull
                            + N' AND NOT EXISTS (SELECT 1 FROM ' + @fkRef + N' r WHERE 1 = 1' + @fkCond + N');'
                            + N' DELETE t FROM ' + @fkTemp + N' t WHERE 1 = 1' + @fkNotNull
                            + N' AND NOT EXISTS (SELECT 1 FROM ' + @fkRef + N' r WHERE 1 = 1' + @fkCond + N');';
                        EXEC (@fkSql);
                    END
                    FETCH NEXT FROM fkCur INTO @fkId, @fkName, @fkRef;
                END
                CLOSE fkCur;
                DEALLOCATE fkCur;
                """.formatted(targetTable, tempTable, filename.replace("'", "''"), rowLabel.replace("'", "''"));
    }

    /** Expression T-SQL : DO_Piece sans son premier caractère numérique (en partant de la gauche). */
    private static String pieceSansPremierChiffre(String alias) {
        return "STUFF(" + alias + ".DO_Piece, PATINDEX('%[0-9]%', " + alias + ".DO_Piece), 1, '')";
    }

    /**
     * T-SQL qui renomme dans F_DOCENTETE_DEST les pièces déjà présentes dans F_DOCENTETE pour un autre document
     * (autre base ou document local) : le premier caractère numérique en partant de la gauche est supprimé.
     */
    public static String renameExistingPieceEntete(String filename) {
        String newPiece = pieceSansPremierChiffre("dest");
        return """
                IF OBJECT_ID('F_DOCENTETE_DEST') IS NOT NULL
                BEGIN
                    INSERT INTO config.DB_Errors
                    SELECT SUSER_SNAME(), NULL, NULL, NULL, NULL, NULL
                         , 'Pièce déjà existante, document renommé'
                         , 'Domaine : ' + CAST(dest.DO_Domaine AS NVARCHAR(10)) + ' Type : ' + CAST(dest.DO_Type AS NVARCHAR(10))
                           + ' Piece : ' + dest.DO_Piece + ' -> ' + %1$s
                           + ' cbMarq : ' + CAST(dest.cbMarqSource AS NVARCHAR(20)) + ' database : ' + ISNULL(dest.DataBaseSource,'')
                           + ' fileName : %2$s'
                         , 'F_DOCENTETE', GETDATE()
                    FROM F_DOCENTETE_DEST dest
                    WHERE PATINDEX('%%[0-9]%%', dest.DO_Piece) > 0
                    AND EXISTS (SELECT 1 FROM F_DOCENTETE src
                                WHERE src.DO_Piece = dest.DO_Piece AND src.DO_Domaine = dest.DO_Domaine AND src.DO_Type = dest.DO_Type
                                AND NOT (ISNULL(src.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                                         AND ISNULL(src.cbMarqSource,0) = ISNULL(dest.cbMarqSource,0)))

                    UPDATE dest SET DO_Piece = %1$s
                    FROM F_DOCENTETE_DEST dest
                    WHERE PATINDEX('%%[0-9]%%', dest.DO_Piece) > 0
                    AND EXISTS (SELECT 1 FROM F_DOCENTETE src
                                WHERE src.DO_Piece = dest.DO_Piece AND src.DO_Domaine = dest.DO_Domaine AND src.DO_Type = dest.DO_Type
                                AND NOT (ISNULL(src.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                                         AND ISNULL(src.cbMarqSource,0) = ISNULL(dest.cbMarqSource,0)))
                END
                """.formatted(newPiece, filename.replace("'", "''"));
    }

    /**
     * T-SQL qui reporte sur une table liée à l'entête (lignes, règlements, échéances, suppressions) le renommage
     * fait par {@link #renameExistingPieceEntete} : la pièce d'origine n'existe pas pour cette base mais la pièce
     * sans son premier chiffre existe.
     */
    public static String renameExistingPieceLink(String destTable) {
        String newPiece = pieceSansPremierChiffre("dest");
        return """
                IF OBJECT_ID('%1$s') IS NOT NULL
                UPDATE dest SET DO_Piece = %2$s
                FROM %1$s dest
                WHERE PATINDEX('%%[0-9]%%', dest.DO_Piece) > 0
                AND NOT EXISTS (SELECT 1 FROM F_DOCENTETE e
                                WHERE e.DO_Piece = dest.DO_Piece AND e.DO_Domaine = dest.DO_Domaine AND e.DO_Type = dest.DO_Type
                                AND ISNULL(e.DataBaseSource,'') = ISNULL(dest.DataBaseSource,''))
                AND EXISTS (SELECT 1 FROM F_DOCENTETE e
                            WHERE e.DO_Piece = %2$s AND e.DO_Domaine = dest.DO_Domaine AND e.DO_Type = dest.DO_Type
                            AND ISNULL(e.DataBaseSource,'') = ISNULL(dest.DataBaseSource,''))
                """.formatted(destTable, newPiece);
    }

    /**
     * URL JDBC à partir du nom de serveur tel qu'on le saisit dans SSMS :
     * "serveur,port" -> "serveur:port", préfixe "tcp:" retiré, "." / "(local)" -> localhost.
     * "serveur\INSTANCE" est déjà compris par le pilote et reste tel quel.
     */
    public static String jdbcUrl(Object serverName, Object database) {
        String server = serverName == null ? "" : serverName.toString().trim();
        if (server.regionMatches(true, 0, "tcp:", 0, 4)) {
            server = server.substring(4).trim();
        }
        int comma = server.lastIndexOf(',');
        String port = "";
        if (comma >= 0) {
            port = server.substring(comma + 1).trim();
            server = server.substring(0, comma).trim();
        }
        if (server.equals(".") || server.equalsIgnoreCase("(local)") || server.equalsIgnoreCase("(localdb)")) {
            server = "localhost";
        } else if (server.startsWith(".\\") || server.regionMatches(true, 0, "(local)\\", 0, 8)) {
            server = "localhost" + server.substring(server.indexOf('\\'));
        }
        return "jdbc:sqlserver://" + server + (port.isEmpty() ? "" : ":" + port) + ";databaseName=" + database;
    }

    public static String applyUpdateFlag(String query, String tableName) {
        return query.replace("{{UPDATE_ENABLED}}", isUpdateEnabled(tableName) ? "1" : "0");
    }

    public static void updateTableDest(Connection sqlCon, String key, String exclude, String tableName, String tableNameDest, String filename, int unibase) {
        if (!isUpdateEnabled(tableName)) {
            System.out.println("Mise à jour des valeurs existantes désactivée pour " + tableName);
            return;
        }
        executeQuery(sqlCon, updateTableDest(key, exclude, tableName, tableNameDest, filename, unibase));
    }

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.s";
    private static Statement stmt;

    public static String selectSourceTable(String table,String dataSource,boolean existsCbModification){
        return "BEGIN \n" +
                "\nDECLARE @TableName AS VARCHAR(100) = '"+table+"'; \n" +
                "\nDECLARE @cbModification VARCHAR(100) = "+ ((!existsCbModification) ? "NULL" : "ISNULL((SELECT CONVERT(NVARCHAR(100),(SELECT MAX(cbModification) FROM " + table + "),25)),'1901-01-01');\n") +
                """

                        DECLARE @lastSynchro VARCHAR(100) = ISNULL((SELECT CONVERT(NVARCHAR(100),(SELECT CASE WHEN ISNULL(isLoaded,0) = 2 THEN lastSynchro ELSE DATEADD(HOUR,-1,lastSynchro) END FROM config.SelectTable WHERE tableName = @TableName),25)),'1901-01-01');IF EXISTS(SELECT 1 FROM config.SelectTable WHERE tableName = @TableName)
                         UPDATE config.SelectTable      SET lastSynchro =  CONVERT(DATETIME,@cbModification,20)
                             , isLoaded =  CASE WHEN CONVERT(DATETIME,@cbModification,20) = lastSynchro THEN                                CASE WHEN ISNULL(isLoaded,0) < 2 THEN  ISNULL(isLoaded,0) + 1 ELSE  ISNULL(isLoaded,0) END                       ELSE 0 END
                         WHERE tableName = @TableName;
                                 ELSE
                                     INSERT INTO config.SelectTable(tableName,lastSynchro,isLoaded) VALUES(@TableName,CONVERT(DATETIME,@cbModification,20),0);
                        DECLARE @MaColonne AS VARCHAR(250);
                        DECLARE @MonSQL AS VARCHAR(MAX)='';
                        DECLARE @databaseSource AS VARCHAR(150) = '%s';
                        DECLARE @getid CURSOR

                        SET @getid = CURSOR FOR
                        SELECT col.name
                        FROM sys.tables tab
                        INNER JOIN sys.columns col
                            ON tab.object_id = col.object_id
                        WHERE tab.name = @TableName
                        AND (col.name NOT LIKE 'cb%%' OR col.Name = 'cbIndice')AND col.name NOT IN ('DataBaseSource','cbMarqSource')

                        OPEN @getid
                        FETCH NEXT
                        FROM @getid INTO @MaColonne
                        WHILE @@FETCH_STATUS = 0
                        BEGIN
                         SELECT @MonSQL = @MonSQL+ ',' + @MaColonne

                         FETCH NEXT
                            FROM @getid INTO @MaColonne --, @name
                        END
                        CLOSE @getid
                        DEALLOCATE @getid
                        SELECT @MonSQL = SUBSTRING(@MonSQL,2,LEN(@MonSQL))

                        SELECT @MonSQL = 'DECLARE @databaseSource AS VARCHAR (150) = '''+@databaseSource+'''; SELECT ' + @MonSQL
                        + CASE WHEN @cbModification IS NOT NULL THEN ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]' ELSE '' END +',cbMarqSource = [cbMarq],[DataBaseSource] = @databaseSource  FROM '
                        + @TableName +  CASE WHEN @cbModification IS NOT NULL THEN ' WHERE cbModification > CONVERT(DATETIME,''' + @lastSynchro +''',20)' ELSE '' END
                        IF EXISTS (    SELECT    col.name
                                    FROM    sys.tables tab
                                    INNER JOIN sys.columns col    ON    tab.object_id = col.object_id
                                    WHERE    tab.name = @TableName
                                    AND        col.name = 'DataBaseSource')
                             SELECT @MonSQL = @MonSQL + ' AND ISNULL(DataBaseSource,''' + @databaseSource +''') = ''' + @databaseSource +''''
                        EXEC(@MonSQL)

                        END\
                        """.formatted(dataSource);
    }

    public static String selectSourceTable(String table, String dataSource, JSONObject type){
        return "BEGIN \n" +
                "DECLARE @TableName AS VARCHAR(100) = '"+table+"'; \n" +
                "DECLARE @cbModification VARCHAR(100) = ISNULL((SELECT CONVERT(NVARCHAR(100),(SELECT MAX(cbModification) FROM " + table + "),25)),'1901-01-01');" +
                "DECLARE @lastSynchro VARCHAR(100) = ISNULL((SELECT CONVERT(NVARCHAR(100),(SELECT CASE WHEN ISNULL(isLoaded,0) = 2 THEN lastSynchro ELSE DATEADD(HOUR,-1,lastSynchro) END FROM config.SelectTable WHERE tableName = @TableName),25)),'1901-01-01');" +

                "IF EXISTS(SELECT 1 FROM config.SelectTable WHERE tableName = @TableName) \n" +
                " UPDATE config.SelectTable " +
                "     SET lastSynchro =  CONVERT(DATETIME,@cbModification,20) \n" +
                "     , isLoaded =  CASE WHEN CONVERT(DATETIME,@cbModification,20) = lastSynchro THEN " +
                "                               CASE WHEN  ISNULL(isLoaded,0) < 2 THEN  ISNULL(isLoaded,0) + 1 ELSE  ISNULL(isLoaded,0) END" +
                "                       ELSE 0 END\n" +
                " WHERE tableName = @TableName; \n" +
                "         ELSE \n" +
                "             INSERT INTO config.SelectTable(tableName,lastSynchro,isLoaded) VALUES(@TableName,CONVERT(DATETIME,@cbModification,20),0); \n"+
                "DECLARE @MaColonne AS VARCHAR(250);\n" +
                "DECLARE @MonSQL AS VARCHAR(MAX)=''; \n" +
                "DECLARE @facturevente AS VARCHAR(1) = "+ type.get("facturedevente") +"; \n" +
                "DECLARE @vente AS VARCHAR(1) = "+ type.get("vente") +"; \n" +
                "DECLARE @stock AS VARCHAR(1) = "+ type.get("stock") +"; \n" +
                "DECLARE @devis AS VARCHAR(1) = "+type.get("devis")+"; \n" +
                "DECLARE @bonlivraison AS VARCHAR(1) = "+type.get("bondelivraison")+"; \n" +
                "DECLARE @factureachat AS VARCHAR(1) = "+type.get("facturedachat")+"; \n" +
                "DECLARE @entree AS VARCHAR(1) = "+type.get("entree")+"; \n" +
                "DECLARE @sortie AS VARCHAR(1) = "+type.get("sortie")+"; \n" +
                "DECLARE @transfert AS VARCHAR(1) = "+type.get("transfert")+"; \n" +
                "DECLARE @interne1 AS VARCHAR(1) = "+type.get("documentinterne1")+"; \n" +
                "DECLARE @interne2 AS VARCHAR(1) = "+type.get("documentinterne2")+"""
                        ;
                        DECLARE @getid CURSOR

                        SET @getid = CURSOR FOR
                        SELECT col.name
                        FROM sys.tables tab
                        INNER JOIN sys.columns col
                            ON tab.object_id = col.object_id
                        WHERE tab.name = @TableName
                        AND col.name NOT LIKE 'cb%%'AND col.name NOT IN ('DataBaseSource','cbMarqSource')

                        OPEN @getid
                        FETCH NEXT
                        FROM @getid INTO @MaColonne
                        WHILE @@FETCH_STATUS = 0
                        BEGIN
                         SELECT @MonSQL = @MonSQL+ ',' + @MaColonne

                         FETCH NEXT
                            FROM @getid INTO @MaColonne --, @name
                        END
                        CLOSE @getid
                        DEALLOCATE @getid
                        SELECT @MonSQL = SUBSTRING(@MonSQL,2,LEN(@MonSQL))

                        SELECT @MonSQL = 'SELECT ' + @MonSQL + ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource = [cbMarq],[DataBaseSource] = ''%s''  FROM '+ @TableName +'  WHERE cbModification > CONVERT(DATETIME,''' + @lastSynchro +''',20)
                          AND CASE WHEN ' + @vente + ' = 1 AND DO_Domaine = 0 THEN 1
                                   WHEN ' + @facturevente + ' = 1 AND DO_Domaine = 0 AND DO_Type IN (6,7) THEN 1
                                   WHEN ' + @devis + ' = 1 AND DO_Domaine = 0 AND DO_Type = 0 THEN 1
                                   WHEN ' + @bonlivraison + ' = 1 AND DO_Domaine = 0 AND DO_Type = 3 THEN 1
                                   WHEN ' + @factureachat + ' = 1 AND DO_Domaine = 1 THEN 1
                                   WHEN ' + @stock + ' = 1 AND DO_Domaine = 2 THEN 1
                                   WHEN ' + @entree + ' = 1 AND DO_Domaine = 2 AND DO_Type = 20 THEN 1
                                   WHEN ' + @sortie + ' = 1 AND DO_Domaine = 2 AND DO_Type = 21 THEN 1
                                   WHEN ' + @transfert + ' = 1 AND DO_Domaine = 2 AND DO_Type = 23 THEN 1
                                   WHEN ' + @interne1 + ' = 1 AND DO_Domaine = 4 AND DO_Type = 40 THEN 1
                                   WHEN ' + @interne2 + ' = 1 AND DO_Domaine = 4 AND DO_Type = 41 THEN 1 END = 1 '

                        IF EXISTS (    SELECT    col.name
                                    FROM    sys.tables tab
                                    INNER JOIN sys.columns col    ON    tab.object_id = col.object_id
                                    WHERE    tab.name = @TableName
                                    AND        col.name = 'DataBaseSource')
                             SELECT @MonSQL = @MonSQL + ' AND ISNULL(DataBaseSource,''%s'') = ''%s'''
                        EXEC(@MonSQL)

                        END\
                        """.formatted(dataSource, dataSource, dataSource);
    }

    public static String selectSourceTableFilterAgency(String table,String dataSource,String agency,String agencyColumn){
        return """
                BEGIN
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX)='';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @getid CURSOR

                SET @getid = CURSOR FOR
                SELECT col.name
                FROM sys.tables tab
                INNER JOIN sys.columns col
                    ON tab.object_id = col.object_id
                WHERE tab.name = @TableName
                AND col.name NOT LIKE 'cb%%'AND col.name NOT IN ('DataBaseSource','cbMarqSource')

                OPEN @getid
                FETCH NEXT
                FROM @getid INTO @MaColonne
                WHILE @@FETCH_STATUS = 0
                BEGIN
                 SELECT @MonSQL = @MonSQL+ ',' + @MaColonne

                 FETCH NEXT
                    FROM @getid INTO @MaColonne --, @name
                END
                CLOSE @getid
                DEALLOCATE @getid
                SELECT @MonSQL = SUBSTRING(@MonSQL,2,LEN(@MonSQL))

                SELECT @MonSQL = 'SELECT ' + @MonSQL + ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource = [cbMarq],[DataBaseSource] = ''%s''  FROM '+ @TableName + ' WHERE  cbModification >= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName='''+ @TableName +'''),''1900-01-01'')' + ' AND    %s IN (SELECT DE_No FROM F_DEPOT WHERE DE_CodePostal = ''%s'')'

                EXEC(@MonSQL)

                END\
                """.formatted(table, dataSource, agencyColumn, agency);
    }

    public static String selectSourceTableFilterAgencyArticle(String table,String dataSource,String agency){
        return """
                BEGIN
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX)='';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @getid CURSOR

                SET @getid = CURSOR FOR
                SELECT col.name
                FROM sys.tables tab
                INNER JOIN sys.columns col
                    ON tab.object_id = col.object_id
                WHERE tab.name = @TableName
                AND CASE WHEN col.name LIKE 'cb%%' THEN 0  WHEN (col.name LIKE '%%_P_MIN' AND LEFT(col.name ,LEN('%s')) <> '%s') THEN 0
                  WHEN (col.name LIKE '%%_QTE_MIN_DG' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN (col.name LIKE '%%_P_MAX' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN (col.name LIKE '%%_P_GROSSISTES' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN (col.name LIKE '%%_QTE_MIN_G' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN (col.name LIKE '%%_SUPER_PRIX' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN (col.name LIKE '%%_QTE_MIN_SUPERPRIX' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN (col.name LIKE '%%_COMMENTAIRES' AND LEFT(col.name ,LEN('%s')) <>  '%s') THEN 0
                  WHEN col.name IN ('DataBaseSource','cbMarqSource') THEN 0
                 ELSE 1 END = 1
                OPEN @getid
                FETCH NEXT
                FROM @getid INTO @MaColonne
                WHILE @@FETCH_STATUS = 0
                BEGIN
                 SELECT @MonSQL = @MonSQL+ ',' + @MaColonne

                 FETCH NEXT
                    FROM @getid INTO @MaColonne --, @name
                END
                CLOSE @getid
                DEALLOCATE @getid
                SELECT @MonSQL = SUBSTRING(@MonSQL,2,LEN(@MonSQL))

                SELECT @MonSQL = 'SELECT ' + @MonSQL + ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource = [cbMarq],[DataBaseSource] = ''%s''  FROM '+ @TableName + ' WHERE  cbModification >= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName='''+ @TableName +'''),''1900-01-01'')' + ''

                EXEC(@MonSQL)

                END\
                """.formatted(table, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, agency, dataSource);
    }

    public static String selectSourceTableFilterAgencyEnteteLink(String table,String dataSource,String agency){
        return """
                BEGIN
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX)='';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @getid CURSOR

                SET @getid = CURSOR FOR
                SELECT col.name
                FROM sys.tables tab
                INNER JOIN sys.columns col
                    ON tab.object_id = col.object_id
                WHERE tab.name = @TableName
                AND col.name NOT LIKE 'cb%%'AND col.name NOT IN ('DataBaseSource','cbMarqSource')

                OPEN @getid
                FETCH NEXT
                FROM @getid INTO @MaColonne
                WHILE @@FETCH_STATUS = 0
                BEGIN
                 SELECT @MonSQL = @MonSQL+ ',' + @MaColonne

                 FETCH NEXT
                    FROM @getid INTO @MaColonne --, @name
                END
                CLOSE @getid
                DEALLOCATE @getid
                SELECT @MonSQL = SUBSTRING(@MonSQL,2,LEN(@MonSQL))

                SELECT @MonSQL = 'SELECT ' + @MonSQL + ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource = [cbMarq],[DataBaseSource] = ''%s''  FROM '+ @TableName + ' WHERE  cbModification >= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName='''+ @TableName +'''),''1900-01-01'')' + ' AND    EXISTS (SELECT  F_DOCENTETE.DO_Domaine,F_DOCENTETE.DO_Piece,F_DOCENTETE.DO_Type                    FROM    F_DOCENTETE                    WHERE   DE_No IN (SELECT DE_No FROM F_DEPOT WHERE DE_CodePostal = ''%s'')                   AND '+ @TableName +'.DO_Domaine = F_DOCENTETE.DO_Domaine                    AND '+ @TableName +'.DO_Type = F_DOCENTETE.DO_Type                   AND '+ @TableName +'.DO_Piece = F_DOCENTETE.DO_Piece)'

                EXEC(@MonSQL)

                END\
                """.formatted(table, dataSource, agency);
    }

    public static String selectSourceTableFilterAgencyRegltLink(String table,String dataSource,String agency){
        return """
                BEGIN
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX)='';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @getid CURSOR

                SET @getid = CURSOR FOR
                SELECT col.name
                FROM sys.tables tab
                INNER JOIN sys.columns col
                    ON tab.object_id = col.object_id
                WHERE tab.name = @TableName
                AND col.name NOT LIKE 'cb%%'AND col.name NOT IN ('DataBaseSource','cbMarqSource')

                OPEN @getid
                FETCH NEXT
                FROM @getid INTO @MaColonne
                WHILE @@FETCH_STATUS = 0
                BEGIN
                 SELECT @MonSQL = @MonSQL+ ',' + @MaColonne

                 FETCH NEXT
                    FROM @getid INTO @MaColonne --, @name
                END
                CLOSE @getid
                DEALLOCATE @getid
                SELECT @MonSQL = SUBSTRING(@MonSQL,2,LEN(@MonSQL))

                SELECT @MonSQL = 'SELECT ' + @MonSQL + ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource = [cbMarq],[DataBaseSource] = ''%s''  FROM '+ @TableName + ' WHERE  cbModification >= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName='''+ @TableName +'''),''1900-01-01'')' + ' AND    EXISTS (SELECT  F_DOCENTETE.DO_Domaine,F_DOCENTETE.DO_Piece,F_DOCENTETE.DO_Type                    FROM    F_DOCENTETE                   INNER JOIN F_REGLECH                       ON  F_REGLECH.DO_Domaine = F_DOCENTETE.DO_Domaine                       AND F_REGLECH.DO_Type = F_DOCENTETE.DO_Type                       AND F_REGLECH.DO_Piece = F_DOCENTETE.DO_Piece                       INNER JOIN F_CREGLEMENT                        ON  F_REGLECH.RG_No = F_CREGLEMENT.RG_No                   WHERE   DE_No IN (SELECT DE_No FROM F_DEPOT WHERE DE_CodePostal = ''%s'')                   AND '+ @TableName +'.RG_No = F_CREGLEMENT.RG_No )'

                EXEC(@MonSQL)

                END\
                """.formatted(table, dataSource, agency);
    }

    public static void listDeleteAllInfo(Connection sqlCon, String path, String file,String table,String listTable,String database)
    {
        String query = listDelete(table,listTable,database);
        writeToFileAvro(path + "\\" + file, query, sqlCon);

        query = listDeleteItem(table,listTable);
        executeQuery(sqlCon, query);
    }

    public static String listDeleteItem(String table,String listTable){
        return " DELETE FROM config."+listTable+" " +
                " WHERE NOT EXISTS(SELECT 1 " +
                "                  FROM "+table +
                "                  WHERE dbo."+table+".cbMarq = config."+listTable+".cbMarq);";
    }

    public static void deleteTempTable(Connection sqlCon,String table)
    {
        String query = """
                IF OBJECT_ID('%s') IS NOT NULL
                    DROP TABLE %s;\
                """.formatted(table, table);
        executeQuery(sqlCon, query);
    }


    public static void loadDeleteFile(String path,Connection sqlCon,String file,String tableName,String query) {
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith("deleteList"+file);
        String [] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                loadDeleteInfo(path, tableName, filename, sqlCon, query);
            }
        }
    }

    public static String [] getFile(String path,String file){
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file) ;
        return dir.list(filter);
    }

    public static void loadDeleteInfo(String path,String tableName,String filename,Connection sqlCon,String query){
        readOnFile(path, filename, tableName + "_SUPPR", sqlCon);
        File file = new File(path + "\\" + filename);
        if (file.exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, filename);
        }
        deleteTempTable(sqlCon, tableName+"_SUPPR");
    }

    public static void deleteItem(Connection sqlCon, String path,String file)
    {
        String query =
                """
                        BEGIN TRY
                        DECLARE @MaColonne AS VARCHAR(250);
                        DECLARE @MonSQL AS VARCHAR(MAX)='';
                        DECLARE @TableName AS VARCHAR(100) = 'F_ARTICLE';
                        DECLARE @isDataSource AS INT = 1;

                        SELECT @MonSQL = 'DELETE FROM '+@TableName+' WHERE EXISTS (SELECT 1 FROM '+@TableName+'_SUPPR WHERE '
                        +@TableName+'.cbMarqSource = '+@TableName+'_SUPPR.cbMarq'

                        IF @isDataSource = 1
                            SELECT @MonSQL = @MonSQL + ' AND '+@TableName+'.DataBaseSource = '+@TableName+'_SUPPR.DataBaseSource)'

                        SELECT @MonSQL = @MonSQL + 'IF OBJECT_ID('''+@TableName+'_SUPPR'') IS NOT NULL DROP TABLE '+@TableName+'_SUPPR'

                        SELECT (@MonSQL)

                        END TRY
                        BEGIN CATCH
                            INSERT INTO config.DB_Errors
                            VALUES
                            (
                                SUSER_SNAME(),
                                ERROR_NUMBER(),
                                ERROR_STATE(),
                                ERROR_SEVERITY(),
                                ERROR_LINE(),
                                ERROR_PROCEDURE(),
                                ERROR_MESSAGE(),
                                @TableName,
                                @MonSQL,
                                GETDATE()
                            );
                        END CATCH
                        """;
        if ((new File(path + "\\deleteList" + file)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + file);
        }
    }

    public static String listDelete(String table,String listTable,String database){
        return  """
                BEGIN TRY
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX)='';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @TableConfig AS VARCHAR(100) = '%s';
                DECLARE @getid CURSOR

                SET @getid = CURSOR FOR
                SELECT col.name
                FROM sys.tables tab
                INNER JOIN sys.columns col
                    ON tab.object_id = col.object_id
                WHERE tab.name = @TableConfig

                OPEN @getid
                FETCH NEXT
                FROM @getid INTO @MaColonne
                WHILE @@FETCH_STATUS = 0
                BEGIN
                 IF @MaColonne ='DataBaseSource'
                 SELECT @MonSQL = @MonSQL+ ',DataBaseSource = ''%s'' '
                 ELSE
                   SELECT @MonSQL = @MonSQL+ ',lart.' + @MaColonne
                 FETCH NEXT
                    FROM @getid INTO @MaColonne
                END
                CLOSE @getid
                DEALLOCATE @getid
                SELECT @MonSQL = 'SELECT '+ SUBSTRING(@MonSQL,2,LEN(@MonSQL))+' FROM config.'+@TableConfig
                +' lart LEFT JOIN '+@TableName+ ' fart ON lart.cbMarq = fart.cbMarq WHERE fart.cbMarq IS NULL ';

                EXEC (@MonSQL)

                END TRY
                BEGIN CATCH
                    INSERT INTO config.DB_Errors
                    VALUES
                    (
                        SUSER_SNAME(),
                        ERROR_NUMBER(),
                        ERROR_STATE(),
                        ERROR_SEVERITY(),
                        ERROR_LINE(),
                        ERROR_PROCEDURE(),
                        ERROR_MESSAGE(),
                        @TableName,
                        @MonSQL,
                        GETDATE()
                    );
                END CATCH
                """.formatted(table, listTable, database);
    }
    public static String updateTableDest(String key,String exclude,String tableName,String tableNameDest,String filename,int unibase){
        String[] keys = key.split(",");
        StringBuilder sql = new StringBuilder("""

                BEGIN TRY
                SET DATEFORMAT ymd;
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX) = '';
                DECLARE @UniBase AS INT = %s;
                DECLARE @isKey AS INT=CASE WHEN '%s' = '' THEN 0 ELSE 1 END;
                DECLARE @Key AS NVARCHAR(250) = '%s';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @TableNameDest AS VARCHAR(100) = '%s';
                DECLARE @getid CURSOR

                SET @getid = CURSOR FOR
                SELECT col.name
                FROM sys.tables tab
                INNER JOIN sys.columns col
                    ON tab.object_id = col.object_id
                WHERE tab.name = @TableName
                AND col.name NOT LIKE 'cb%%'
                AND col.name NOT IN (%s)
                -- seulement les colonnes présentes aussi dans la table reçue (la base source peut en avoir moins)
                AND EXISTS (SELECT 1 FROM sys.columns colDest
                            WHERE colDest.object_id = OBJECT_ID(@TableNameDest) AND colDest.name = col.name)

                OPEN @getid
                FETCH NEXT
                FROM @getid INTO @MaColonne
                WHILE @@FETCH_STATUS = 0
                BEGIN
                 SELECT @MonSQL = @MonSQL+ ',' + @MaColonne +' = ' + @TableNameDest + '.' + @MaColonne
                 FETCH NEXT
                    FROM @getid INTO @MaColonne
                END
                CLOSE @getid
                DEALLOCATE @getid
                 SELECT @MonSQL = 'IF OBJECT_ID('''+@TableNameDest+''') IS NOT NULL UPDATE '+@TableName+' SET '+ SUBSTRING(@MonSQL,2,LEN(@MonSQL));
                 SELECT @MonSQL = @MonSQL + ' FROM ' + @TableNameDest  +' WHERE '
                IF @isKey = 0
                    SELECT @MonSQL = @MonSQL + @TableName + '.cbMarqSource = '+ @TableNameDest+'.cbMarqSource '
                                        +' AND CASE WHEN '+CAST(@UniBase AS VARCHAR(150))+'= 0 AND '+@TableName + '.DataBaseSource = '+ @TableNameDest+'.DataBaseSource THEN 1'
                                        +'             WHEN '+CAST(@UniBase AS VARCHAR(150))+'= 1 THEN 1 ELSE 0 END = 1'
                ELSE
                """.formatted(unibase, key, key, tableName, tableNameDest, exclude));
        for (int i = 0;i< keys.length;i++) {
            if(i== 0 ) {
                sql.append(" SELECT @MonSQL = @MonSQL ");
                sql.append("+ @TableName + '.").append(keys[i]).append(" = '+ @TableNameDest+'.").append(keys[i]).append(" '");
            }
            if(i>0)
                sql.append("+' AND '+@TableName + '.").append(keys[i]).append(" = '+ @TableNameDest+'.").append(keys[i]).append(" '");
        }
        sql.append("\n").append("EXEC (@MonSQL) \n").append("\n").append("END TRY\n").append("BEGIN CATCH \n").append("\tINSERT INTO config.DB_Errors\n").append("\tVALUES\n").append("\t(\n").append("\t\tSUSER_SNAME(),\n").append("\t\tERROR_NUMBER(),\n").append("\t\tERROR_STATE(),\n").append("\t\tERROR_SEVERITY(),\n").append("\t\tERROR_LINE(),\n").append("\t\tERROR_PROCEDURE(),\n").append("\t\tERROR_MESSAGE(),\n").append("\t\t@TableName + ' ").append(filename).append("',\n").append("\t\t@MonSQL,\n").append("\t\tGETDATE()\n").append("\t);\n").append("END CATCH");
        return sql.toString();
    }
    // Réception (UpdateData) : c'est le zip reçu qui est archivé, les fichiers décompressés sont supprimés une fois traités
    private static boolean deleteProcessedFiles = false;

    public static void setDeleteProcessedFiles(boolean delete) {
        deleteProcessedFiles = delete;
    }

    public static void archiveDocument(String archive, String source,String file)
    {
        if (deleteProcessedFiles) {
            try {
                Files.deleteIfExists(Paths.get(source + "\\" + file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        moveToArchive(archive, source, file);
    }

    // Première date aaaammjj valide trouvée dans un nom de fichier (ex. file_20261017001351.zip)
    private static final java.util.regex.Pattern FILE_DATE = java.util.regex.Pattern.compile("(20\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])");

    // Déplace le fichier dans archive\aaaa\mm\jj. La date vient du nom du fichier,
    // ou à défaut de sa date de modification. Une erreur d'archivage n'interrompt pas le traitement.
    public static void moveToArchive(String archive, String source,String file)
    {
        Path sourceFile = Paths.get(source, file);
        try {
            String year, month, day;
            java.util.regex.Matcher m = FILE_DATE.matcher(file);
            if (m.find()) {
                year = m.group(1);
                month = m.group(2);
                day = m.group(3);
            } else {
                LocalDate modified = LocalDate.ofInstant(Files.getLastModifiedTime(sourceFile).toInstant(), java.time.ZoneId.systemDefault());
                year = String.format("%04d", modified.getYear());
                month = String.format("%02d", modified.getMonthValue());
                day = String.format("%02d", modified.getDayOfMonth());
            }
            Path target = Paths.get(archive, year, month, day);
            Files.createDirectories(target);
            Files.move(sourceFile, target.resolve(file), REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            System.out.println("Archivage impossible de " + sourceFile + " : " + e);
        }
    }

    public static void writeOnDb(Connection sqlCon, String path, String fileName, String query)
    {

        File filePath = new File(path + "\\" + fileName);
        if (filePath.exists())
        {
            executeQuery(sqlCon,query);
            archiveDocument(path + "\\archive", path, fileName);
        }
    }

    public static void sendData(Connection sqlCon, String path,String file,String query)
    {
        writeOnDb(sqlCon, path, file, query);
    }


    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Nombre de lignes envoyées à SQL Server par lot lors de l'import Avro (bulk copy)
    private static final int AVRO_BULK_BATCH_SIZE = 50000;

    private static final Conversions.DecimalConversion DECIMAL_CONVERSION = new Conversions.DecimalConversion();

    // Schéma Avro (non nul) correspondant à la colonne i du ResultSet, à partir du type JDBC
    private static org.apache.avro.Schema mapSQLTypeToAvroSchema(ResultSetMetaData metaData, int i) throws SQLException {
        switch (metaData.getColumnType(i)) {
            case Types.BIT:
            case Types.BOOLEAN:
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.BOOLEAN);
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.INT);
            case Types.BIGINT:
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.LONG);
            case Types.REAL:
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.FLOAT);
            case Types.FLOAT:
            case Types.DOUBLE:
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.DOUBLE);
            case Types.DECIMAL:
            case Types.NUMERIC: // inclut money / smallmoney
                int precision = metaData.getPrecision(i);
                int scale = Math.max(metaData.getScale(i), 0);
                if (precision <= 0 || scale > precision) {
                    return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.STRING);
                }
                return LogicalTypes.decimal(precision, scale)
                        .addToSchema(org.apache.avro.Schema.create(org.apache.avro.Schema.Type.BYTES));
            case Types.DATE:
                return LogicalTypes.date()
                        .addToSchema(org.apache.avro.Schema.create(org.apache.avro.Schema.Type.INT));
            case Types.TIMESTAMP: // datetime, smalldatetime, datetime2
                return LogicalTypes.localTimestampMillis()
                        .addToSchema(org.apache.avro.Schema.create(org.apache.avro.Schema.Type.LONG));
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: // varbinary, image, timestamp/rowversion
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.BYTES);
            default:
                org.apache.avro.Schema string = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.STRING);
                // taille de la colonne source, pour recréer NVARCHAR(n) à l'import au lieu de NVARCHAR(MAX)
                string.addProp(SQL_LENGTH_PROP, metaData.getPrecision(i));
                return string;
        }
    }

    // Propriété Avro portant la taille SQL d'une colonne texte
    private static final String SQL_LENGTH_PROP = "sqlLength";
    // Taille utilisée pour les fichiers produits avant l'ajout de sqlLength
    private static final int DEFAULT_STRING_LENGTH = 4000;

    // Taille NVARCHAR d'une colonne texte : 1..4000, ou -1 pour NVARCHAR(MAX).
    // NVARCHAR(MAX) est envoyé par morceaux par le pilote et rend l'import environ 4 fois plus lent.
    private static int stringLength(org.apache.avro.Schema schema) {
        Object prop = schema.getObjectProp(SQL_LENGTH_PROP);
        if (!(prop instanceof Number)) return DEFAULT_STRING_LENGTH;
        int length = ((Number) prop).intValue();
        return (length > 0 && length <= 4000) ? length : -1;
    }

    // Lit la colonne i du ResultSet et la convertit dans la représentation Avro du schéma donné
    private static Object readAvroValue(ResultSet resultSet, int i, org.apache.avro.Schema schema) throws SQLException {
        LogicalType logicalType = schema.getLogicalType();
        if (logicalType instanceof LogicalTypes.Decimal) {
            BigDecimal value = resultSet.getBigDecimal(i);
            if (value == null) return null;
            int scale = ((LogicalTypes.Decimal) logicalType).getScale();
            return DECIMAL_CONVERSION.toBytes(value.setScale(scale, RoundingMode.HALF_UP), schema, logicalType);
        }
        if (logicalType instanceof LogicalTypes.Date) {
            java.sql.Date value = resultSet.getDate(i);
            return value == null ? null : (int) value.toLocalDate().toEpochDay();
        }
        if (logicalType instanceof LogicalTypes.LocalTimestampMillis) {
            Timestamp value = resultSet.getTimestamp(i);
            // Heure locale sans fuseau : restituée telle quelle quel que soit le fuseau du serveur cible
            return value == null ? null : value.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        Object value;
        switch (schema.getType()) {
            case BOOLEAN: value = resultSet.getBoolean(i); break;
            case INT:     value = resultSet.getInt(i); break;
            case LONG:    value = resultSet.getLong(i); break;
            case FLOAT:   value = resultSet.getFloat(i); break;
            case DOUBLE:  value = resultSet.getDouble(i); break;
            case BYTES:
                byte[] bytes = resultSet.getBytes(i);
                return bytes == null ? null : ByteBuffer.wrap(bytes);
            default:
                return resultSet.getString(i);
        }
        return resultSet.wasNull() ? null : value;
    }

    // Méthode principale pour écrire le résultat de la requête dans un fichier Avro
    public static void writeToFileAvro(String fileName, String query, Connection sqlCon) {
        File file = new File(fileName.replace(".csv", ".avro"));
        try (Statement statement = sqlCon.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Construire le schéma Avro à partir des métadonnées (toutes les colonnes sont nullables)
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            org.apache.avro.Schema[] columnSchemas = new org.apache.avro.Schema[columnCount + 1];
            List<org.apache.avro.Schema.Field> fields = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                columnSchemas[i] = mapSQLTypeToAvroSchema(metaData, i);
                org.apache.avro.Schema nullable = org.apache.avro.Schema.createUnion(
                        org.apache.avro.Schema.create(org.apache.avro.Schema.Type.NULL), columnSchemas[i]);
                fields.add(new org.apache.avro.Schema.Field(metaData.getColumnLabel(i), nullable, null,
                        org.apache.avro.Schema.Field.NULL_DEFAULT_VALUE));
            }
            org.apache.avro.Schema schema = org.apache.avro.Schema.createRecord("ResultSetRecord", null, null, false, fields);

            try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<GenericRecord>(schema))) {
                dataFileWriter.setCodec(CodecFactory.deflateCodec(6));
                dataFileWriter.create(schema, file);

                while (resultSet.next()) {
                    GenericRecord record = new GenericData.Record(schema);
                    for (int i = 1; i <= columnCount; i++) {
                        record.put(i - 1, readAvroValue(resultSet, i, columnSchemas[i]));
                    }
                    dataFileWriter.append(record);
                }
            }

        } catch (Exception e) {
            // Ne jamais laisser un fichier partiel qui serait envoyé puis importé
            if (file.exists() && !file.delete()) {
                System.err.println("Impossible de supprimer le fichier Avro incomplet : " + file);
            }
            throw new RuntimeException("Échec de l'export Avro vers " + file, e);
        }
    }


    public static void writeOnFile(String fileName, String query, Connection sqlCon)
    {
        ResultSet result;
        FileWriter fileWriter;
        try {
            result = executeQueryResult(sqlCon,query);

            if(result.isBeforeFirst()) {          //res.isBeforeFirst() is true if the cursor
                fileWriter = new FileWriter(fileName, StandardCharsets.ISO_8859_1);
                int columnCount = writeHeaderLine(result, fileWriter);
                while (result.next()) {
                    String line = "";

                    for (int i = 1; i <= columnCount; i++) {
                        Object valueObject = result.getObject(i);
                        String valueString = "";

                        if (valueObject != null) valueString = valueObject.toString();

                        if (valueObject instanceof String) {
                            valueString = "\"" + escapeDoubleQuotes(valueString) + "\"";
                        }

                        if (isDateValid(valueString)) {
                            valueString = "\"" + valueString.replace(".0", "") + "\"";
                        }

                        line = line.concat(valueString);

                        if (i != columnCount) {
                            line = line.concat(";");
                        }
                    }

                    fileWriter.write(line + "\n");
                    fileWriter.flush();
                }
                fileWriter.close();
                stmt.close();
            }
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    // Retourne le type non nul d'une union ["null", type]
    private static org.apache.avro.Schema nonNullSchema(org.apache.avro.Schema schema) {
        if (schema.getType() == org.apache.avro.Schema.Type.UNION) {
            for (org.apache.avro.Schema type : schema.getTypes()) {
                if (type.getType() != org.apache.avro.Schema.Type.NULL) {
                    return type;
                }
            }
        }
        return schema;
    }

    // Method to map Avro types to SQL Server types
    private static String mapAvroTypeToSQLType(org.apache.avro.Schema schema) {
        schema = nonNullSchema(schema);
        LogicalType logicalType = schema.getLogicalType();
        if (logicalType instanceof LogicalTypes.Decimal) {
            LogicalTypes.Decimal decimal = (LogicalTypes.Decimal) logicalType;
            return "DECIMAL(" + Math.min(decimal.getPrecision(), 38) + "," + decimal.getScale() + ")";
        }
        if (logicalType instanceof LogicalTypes.Date) {
            return "DATE";
        }
        if (logicalType instanceof LogicalTypes.LocalTimestampMillis) {
            return "DATETIME2(3)";
        }

        switch (schema.getType()) {
            case INT:
                return "INT";
            case LONG:
                return "BIGINT";
            case FLOAT:
                return "REAL";
            case DOUBLE:
                return "FLOAT";
            case BOOLEAN:
                return "BIT";
            case BYTES:
                return "VARBINARY(MAX)";
            default:
                int length = stringLength(schema);
                return length > 0 ? "NVARCHAR(" + length + ")" : "NVARCHAR(MAX)";
        }
    }


    // Lire le fichier Avro et insérer les données dans la base de données.
    // Tout se fait dans une transaction : en cas d'erreur la table cible revient à son état précédent.
    public static void insertAvroDataToSqlServer(String avroFilePath, String tableName, Connection conn) {
        File avroFile = new File(avroFilePath);
        if (!avroFile.exists()) {
            System.out.println("Fichier Avro absent, import ignoré : " + avroFilePath);
            return;
        }

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (org.apache.avro.file.FileReader<GenericRecord> dataFileReader =
                         DataFileReader.openReader(avroFile, new GenericDatumReader<GenericRecord>())) {
                org.apache.avro.Schema schema = dataFileReader.getSchema();

                createTableFromSchema(conn, schema, tableName);

                // Bulk copy (protocole BULK INSERT de SQL Server) : beaucoup plus rapide que des INSERT par lots
                SQLServerBulkCopyOptions options = new SQLServerBulkCopyOptions();
                options.setBatchSize(AVRO_BULK_BATCH_SIZE);
                options.setBulkCopyTimeout(0);
                options.setTableLock(true);
                try (SQLServerBulkCopy bulkCopy = new SQLServerBulkCopy(conn)) {
                    bulkCopy.setBulkCopyOptions(options);
                    bulkCopy.setDestinationTableName("[" + tableName + "]");
                    bulkCopy.writeToServer(new AvroBulkData(dataFileReader, schema));
                }
            }
            conn.commit();

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw new RuntimeException("Échec de l'import Avro de " + avroFilePath + " dans " + tableName, e);
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to create a SQL Server table from Avro schema
    private static void createTableFromSchema(Connection conn, org.apache.avro.Schema schema, String tableName) throws SQLException {
        StringBuilder createTableQuery = new StringBuilder("DROP TABLE IF EXISTS [").append(tableName).append("]; CREATE TABLE [");
        createTableQuery.append(tableName).append("] (");

        for (org.apache.avro.Schema.Field field : schema.getFields()) {
            createTableQuery.append("[").append(field.name()).append("] ");
            createTableQuery.append(mapAvroTypeToSQLType(field.schema())).append(",");
        }

        // Remove the last comma
        createTableQuery.setLength(createTableQuery.length() - 1);
        createTableQuery.append(");");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableQuery.toString());
        }
    }

    // Convertit une valeur Avro (schéma non nul) en valeur JDBC
    private static Object toJdbcValue(Object value, org.apache.avro.Schema fieldSchema) {
        if (value == null) return null;
        LogicalType logicalType = fieldSchema.getLogicalType();
        if (logicalType instanceof LogicalTypes.Decimal) {
            return DECIMAL_CONVERSION.fromBytes(((ByteBuffer) value).duplicate(), fieldSchema, logicalType);
        }
        if (logicalType instanceof LogicalTypes.Date) {
            return java.sql.Date.valueOf(LocalDate.ofEpochDay((Integer) value));
        }
        if (logicalType instanceof LogicalTypes.LocalTimestampMillis) {
            long millis = (Long) value;
            return Timestamp.valueOf(LocalDateTime.ofEpochSecond(Math.floorDiv(millis, 1000L),
                    (int) Math.floorMod(millis, 1000L) * 1_000_000, ZoneOffset.UTC));
        }
        switch (fieldSchema.getType()) {
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
                return value;
            case BYTES:
                ByteBuffer buffer = ((ByteBuffer) value).duplicate();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                return bytes;
            default:
                return value.toString();
        }
    }

    // Source du bulk copy : lit les enregistrements du fichier Avro au fil de l'eau
    private static final class AvroBulkData implements ISQLServerBulkData {
        private final transient org.apache.avro.file.FileReader<GenericRecord> reader;
        private final transient org.apache.avro.Schema[] fieldSchemas;
        private final String[] names;
        private final int[] types;
        private final int[] precisions;
        private final int[] scales;
        private final Set<Integer> ordinals = new LinkedHashSet<>();
        private transient GenericRecord record;

        AvroBulkData(org.apache.avro.file.FileReader<GenericRecord> reader, org.apache.avro.Schema schema) {
            this.reader = reader;
            List<org.apache.avro.Schema.Field> fields = schema.getFields();
            int n = fields.size();
            fieldSchemas = new org.apache.avro.Schema[n];
            names = new String[n];
            types = new int[n];
            precisions = new int[n];
            scales = new int[n];
            for (int i = 0; i < n; i++) {
                org.apache.avro.Schema fieldSchema = nonNullSchema(fields.get(i).schema());
                fieldSchemas[i] = fieldSchema;
                names[i] = fields.get(i).name();
                types[i] = getSqlType(fieldSchema);
                LogicalType logicalType = fieldSchema.getLogicalType();
                if (logicalType instanceof LogicalTypes.Decimal) {
                    precisions[i] = ((LogicalTypes.Decimal) logicalType).getPrecision();
                    scales[i] = ((LogicalTypes.Decimal) logicalType).getScale();
                } else if (types[i] == Types.TIMESTAMP) {
                    precisions[i] = 23;
                    scales[i] = 3;
                } else if (types[i] == Types.NVARCHAR || types[i] == Types.VARCHAR) {
                    int length = stringLength(fieldSchema);
                    precisions[i] = length > 0 ? length : Integer.MAX_VALUE;
                } else {
                    precisions[i] = types[i] == Types.VARBINARY ? Integer.MAX_VALUE : 0;
                }
                ordinals.add(i + 1);
            }
        }

        @Override public Set<Integer> getColumnOrdinals() { return ordinals; }
        @Override public String getColumnName(int column) { return names[column - 1]; }
        @Override public int getColumnType(int column) { return types[column - 1]; }
        @Override public int getPrecision(int column) { return precisions[column - 1]; }
        @Override public int getScale(int column) { return scales[column - 1]; }

        @Override
        public boolean next() throws SQLException {
            if (!reader.hasNext()) return false;
            try {
                record = reader.next(record);
            } catch (IOException e) {
                throw new SQLException("Lecture Avro impossible", e);
            }
            return true;
        }

        @Override
        public Object[] getRowData() {
            Object[] row = new Object[names.length];
            for (int i = 0; i < row.length; i++) {
                row[i] = toJdbcValue(record.get(i), fieldSchemas[i]);
            }
            return row;
        }
    }

    // Helper method to get SQL type from Avro schema (non nul)
    private static int getSqlType(org.apache.avro.Schema schema) {
        LogicalType logicalType = schema.getLogicalType();
        if (logicalType instanceof LogicalTypes.Decimal) return Types.DECIMAL;
        if (logicalType instanceof LogicalTypes.Date) return Types.DATE;
        if (logicalType instanceof LogicalTypes.LocalTimestampMillis) return Types.TIMESTAMP;

        switch (schema.getType()) {
            case INT:
                return Types.INTEGER;
            case LONG:
                return Types.BIGINT;
            case FLOAT:
                return Types.REAL;
            case DOUBLE:
                return Types.DOUBLE;
            case BOOLEAN:
                return Types.BIT;
            case STRING:
                return Types.NVARCHAR;
            case BYTES:
                return Types.VARBINARY;
            default:
                return Types.VARCHAR;
        }
    }

    public static void readOnFile(String path,String fileInfo,String table,Connection sqlCon)
    {
        // Seuls les fichiers Avro sont importés (un éventuel .csv ancien format est ignoré)
        if (!fileInfo.endsWith(".avro")) {
            System.out.println("Fichier non Avro ignoré : " + fileInfo);
            return;
        }
        insertAvroDataToSqlServer(path.concat("\\").concat(fileInfo),table,sqlCon);
        /*
        String sql;
        StringBuilder sqlCreateTable;
        String header = "";
        String lineHeader;
        String fileName = path+"\\"+fileInfo;
        try {
            File file = new File(fileName);
            if(file.exists()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.ISO_8859_1));
                int i = 0;
                while ((line = br.readLine()) != null) {
                    if (i == 0) {
                        header = "INSERT INTO " + table + "(";
                        lineHeader = line;
                        line = line.replace(";", ",").replace("\"", "");

                        sqlCreateTable = new StringBuilder("IF OBJECT_ID('" + table + "') IS NOT NULL \n" +
                                "\tDROP TABLE " + table + "\n" +
                                "\n" +
                                "SELECT ");
                        String[] value = lineHeader.split(";");
                        for (String data : value)
                            sqlCreateTable.append(" ").append(data).append(" = CAST('' AS NVARCHAR(150)),");

                        sqlCreateTable = new StringBuilder(sqlCreateTable.substring(0, sqlCreateTable.length() - 1));

                        sqlCreateTable.append(" INTO ").append(table).append(   /*" FROM " + table.replace("_DEST", "") +*/ /*";").append(" TRUNCATE TABLE ").append(table);
                        executeQuery(sqlCon, sqlCreateTable.toString());
                        header = header + line + ")";
                    } else {
                        sql = header + "VALUES (";
                        String[] value = line.split(";");
                        for (String data : value) {
                            if (data.contains("\"")) {
                                data = data.replace("'", "''");
                                sql = sql.concat(data).replace("\"", "'").concat(",");
                            } else if (data.equals("")) {
                                sql = sql.concat("NULL").concat(",");
                            }else
                                sql = sql.concat(data).concat(",");
                        }
                        sql = " IF OBJECTPROPERTY(OBJECT_ID('" + table + "'), 'TableHasIdentity') = 1 SET IDENTITY_INSERT " + table + " ON ; "
                                + sql.substring(0, sql.length() - 1) + "); "
                                + " IF OBJECTPROPERTY(OBJECT_ID('" + table + "'), 'TableHasIdentity') = 1  SET IDENTITY_INSERT " + table + " OFF ; ";
                       executeQuery(sqlCon,sql);
                    }
                    i++;
                }
                br.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    private static String escapeDoubleQuotes(String value) {
        return value.replaceAll("\"", "\"\"");
    }

    private static int writeHeaderLine(ResultSet result,FileWriter fileWriter) throws SQLException, IOException {
        // write header line containing column names
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();
        String headerLine = "";

        // exclude the first column which is the ID field
        for (int i = 1; i <= numberOfColumns; i++) {
            String columnName = metaData.getColumnName(i);
            headerLine = headerLine.concat(columnName).concat(";");
        }

        fileWriter.write(headerLine.substring(0, headerLine.length() - 1)+"\n");

        return numberOfColumns;
    }

    public static void updateSelectTable(String table, Connection sqlCon)
    {
        executeQuery(sqlCon,updateSelectTableQuery(table));
    }

    public static String updateSelectTableQuery(String table)
    {
        return  """
                DECLARE @cbModification DATETIME = (SELECT MAX(cbModification) FROM %s);DECLARE @table VARCHAR(150) = '%s'IF EXISTS(SELECT 1 FROM config.SelectTable WHERE tableName = @table)
                 UPDATE config.SelectTable      SET lastSynchro =  @cbModification
                 WHERE tableName = @table;
                         ELSE
                             INSERT INTO config.SelectTable(tableName,lastSynchro) VALUES(@table,@cbModification)
                """.formatted(table, table);
    }

    public static void getData(Connection sqlCon, String query,String table,String path,String file)
    {
        //updateSelectTable(table, sqlCon);

        writeToFileAvro(path + "\\" + file, query, sqlCon);
        //writeOnFile(path+"\\"+file, query, sqlCon);
    }

    public static void executeQuery(Connection sqlCon, String query)
    {
        try {
            stmt = sqlCon.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void disableTrigger (Connection sqlCon,String table){
        executeQuery(sqlCon,
                "                DISABLE TRIGGER ALL ON [dbo].["+table+"] ;\n");
    }

    public static void enableTrigger (Connection sqlCon,String table){
        executeQuery(sqlCon,
                "                ENABLE TRIGGER ALL ON [dbo].["+table+"] ;\n");
    }

    public static ResultSet executeQueryResult(Connection sqlCon, String query)
    {
        ResultSet resultSet = null;
        try {
            stmt = sqlCon.createStatement();
            resultSet = stmt.executeQuery(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }


    public static void initTableParam(Connection sqlCon,String table,String configTable,String keyColumn)
    {
        String query = """
                 BEGIN TRY
                DECLARE @MaColonne AS VARCHAR(250);
                DECLARE @MonSQL AS VARCHAR(MAX)='';
                DECLARE @TableName AS VARCHAR(100) = '%s';
                DECLARE @configTable AS VARCHAR(100) = '%s';
                DECLARE @keyColumn AS VARCHAR(250) = '%s';

                -- colonnes nommées : une colonne ajoutée plus tard (ALTER TABLE) se retrouve en fin de table
                SELECT @MonSQL = ' INSERT INTO config.'+@configTable+' ('+@keyColumn+',cbMarq,DateCreation) SELECT '+@keyColumn+',cbMarq,GETDATE() FROM ( SELECT DISTINCT '+@keyColumn+',cbMarq FROM '+@TableName
                +' EXCEPT SELECT '+@keyColumn+',cbMarq FROM config.'+@configTable + ')A'
                EXEC (@MonSQL)

                END TRY
                BEGIN CATCH
                    INSERT INTO config.DB_Errors
                    VALUES
                    (
                        SUSER_SNAME(),
                        ERROR_NUMBER(),
                        ERROR_STATE(),
                        ERROR_SEVERITY(),
                        ERROR_LINE(),
                        ERROR_PROCEDURE(),
                        ERROR_MESSAGE(),
                        @TableName,
                        @MonSQL,
                        GETDATE()
                    );
                END CATCH
                """.formatted(table, configTable, keyColumn);
        executeQuery(sqlCon, query);
    }
}
