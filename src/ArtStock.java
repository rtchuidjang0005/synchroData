import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class ArtStock extends Table {

    public static String file = "ArtStock_";
    public static String tableName = "F_ARTSTOCK";
    public static String configList = "listArtStock";

    public static String insert(String filename)
    {
        return          """
                BEGIN TRY
                SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_ARTSTOCK_DEST') IS NOT NULL AND {{UPDATE_ENABLED}} = 1
                WITH GlobalTable AS (
                SELECT    dest.*
                        ,DE_NoFinal =dep.DE_No
                        ,depempl.DP_No
                FROM    F_ARTSTOCK_DEST dest
                INNER JOIN F_DEPOT dep
                    ON    dep.DE_NoSource = dest.DE_No
                    AND dep.DatabaseSource = dest.DatabaseSource
                LEFT JOIN F_DEPOTEMPL depempl
                    ON    depempl.DP_NoSource = dest.DP_NoPrincipal
                    AND depempl.DatabaseSource = dest.DatabaseSource
                )
                UPDATE upd SET
                            AS_QteMini = dest.AS_QteMini
                            ,AS_QteMaxi = dest.AS_QteMaxi
                            ,AS_MontSto = dest.AS_MontSto
                            ,AS_QteSto = dest.AS_QteSto
                            ,AS_QteRes = dest.AS_QteRes
                            ,AS_QteCom = dest.AS_QteCom
                            ,AS_Principal = dest.AS_Principal
                            ,AS_QteResCM = dest.AS_QteResCM
                            ,AS_QteComCM = dest.AS_QteComCM
                            ,AS_QtePrepa = dest.AS_QtePrepa
                            ,AS_QteAControler = dest.AS_QteAControler
                            ,DP_NoPrincipal = dest.DP_No
                            ,cbProt = dest.cbProt
                            ,cbModification = dest.cbModification
                            ,cbFlag = dest.cbFlag
                FROM F_ARTSTOCK upd
                INNER JOIN GlobalTable dest
                    ON    upd.DE_No = dest.DE_NoFinal
                    AND upd.AR_Ref = dest.AR_Ref;
                INSERT INTO [dbo].[F_ARTSTOCK]
                ([AR_Ref],[DE_No],[AS_QteMini],[AS_QteMaxi],[AS_MontSto],[AS_QteSto],[AS_QteRes],[AS_QteCom],[AS_Principal],[AS_QteResCM],[AS_QteComCM]
                      ,[AS_QtePrepa],[DP_NoPrincipal],[DP_NoControle],[AS_QteAControler],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                SELECT    dest.[AR_Ref]
                        ,dep.DE_No,[AS_QteMini],[AS_QteMaxi]
                        ,[AS_MontSto],[AS_QteSto],[AS_QteRes],[AS_QteCom],[AS_Principal],[AS_QteResCM],[AS_QteComCM]
                        ,[AS_QtePrepa]
                        ,[DP_NoPrincipal] = ISNULL(depempl.[DP_No],dest.[DP_NoPrincipal])
                        ,[DP_NoControle]
                        ,[AS_QteAControler],dest.[cbProt],dest.[cbCreateur]
                        ,dest.[cbModification],dest.[cbReplication],dest.[cbFlag]
                FROM F_ARTSTOCK_DEST dest
                INNER JOIN F_DEPOT dep
                    ON    dep.DE_NoSource = dest.DE_No
                    AND dep.DatabaseSource = dest.DatabaseSource
                LEFT JOIN F_DEPOTEMPL depempl
                    ON    depempl.DP_NoSource = dest.DP_NoPrincipal
                    AND depempl.DatabaseSource = dest.DatabaseSource
                WHERE NOT EXISTS (    SELECT *
                                    FROM  F_ARTSTOCK a
                                    WHERE a.DE_No = dep.DE_No
                                    AND      a.AR_Ref = dest.AR_Ref);
                IF OBJECT_ID('F_ARTSTOCK_DEST') IS NOT NULL
                DROP TABLE F_ARTSTOCK_DEST;
                 END TRY
                 BEGIN CATCH
                INSERT INTO config.DB_Errors
                    VALUES
                  (SUSER_SNAME(),
                   ERROR_NUMBER(),
                   ERROR_STATE(),
                   ERROR_SEVERITY(),
                   ERROR_LINE(),
                   ERROR_PROCEDURE(),
                   ERROR_MESSAGE(),
                   'Insert '+ ' %s',
                   'F_ARTSTOCK',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
    }

    public static void sendDataElement(Connection  sqlCon, String path,String database,int unibase)
    {
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file);
        String[] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                readOnFile(path, "deleteList" + filename, tableName + "_SUPPR", sqlCon);
                //executeQuery(sqlCon, updateTableDest("AR_Ref,DE_No", "'AR_Ref','DE_No'", tableName, tableName + "_DEST"));
                sendData(sqlCon, path, filename, applyUpdateFlag(insert(filename), tableName));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteArtStock(sqlCon, path, filename, database);
            }
        }
    }

    public static String selectSourceTable(String table,String dataSource){
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

                SELECT @MonSQL = 'SELECT ' + @MonSQL + ',[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource = [cbMarq],[DataBaseSource] = ''%s''  FROM '+ @TableName IF EXISTS (    SELECT    col.name
                            FROM    sys.tables tab
                            INNER JOIN sys.columns col    ON    tab.object_id = col.object_id
                            WHERE    tab.name = @TableName
                            AND        col.name = 'DataBaseSource')
                     SELECT @MonSQL = @MonSQL + ' AND ISNULL(DataBaseSource,''%s'') = ''%s'''
                EXEC(@MonSQL)

                END\
                """.formatted(table, dataSource, dataSource, dataSource);
    }

    public static void getDataElement(Connection  sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref,DE_No");
        getData(sqlCon, selectSourceTable(tableName,database), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void getDataElementFilterAgency(Connection  sqlCon, String path,String database,String time,String agency)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref,DE_No");
        getData(sqlCon, selectSourceTableFilterAgency(tableName,database,agency,"DE_No"), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArtStock(Connection sqlCon, String path,String filename,String database)
    {
        String query =
                """
                         DELETE FROM F_ARTSTOCK
                         WHERE EXISTS (SELECT 1 FROM F_ARTSTOCK_SUPPR artSuppr
                               INNER JOIN F_DEPOT dep
                               ON  artSuppr.DE_No = dep.DE_NoSource
                               AND dep.dataBaseSource = '%s'
                               WHERE artSuppr.AR_Ref = F_ARTSTOCK.AR_Ref       AND dep.DE_No = F_ARTSTOCK.DE_No)

                         IF OBJECT_ID('F_ARTSTOCK_SUPPR') IS NOT NULL
                         DROP TABLE F_ARTSTOCK_SUPPR ;\
                        """.formatted(database);
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
