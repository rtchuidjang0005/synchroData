import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class ArticleRessource extends Table {

    public static String file ="articleressource_";
    public static String tableName = "F_ARTICLERESSOURCE";
    public static String configList = "ListArticleRessource";
    public static String list()
    {
        return """
                SELECT    [AR_Ref],[RP_Code],[cbProt],[cbMarq]
                        ,[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                 ,cbMarqSource = cbMarq  FROM    [F_ARTICLERESSOURCE] WHERE cbModification >= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName='F_ARTICLERESSOURCE'),'1900-01-01')\
                """;
    }

    public static String insert(String filename)
    {
        return """
                BEGIN TRY SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_ARTICLERESSOURCE_DEST') IS NOT NULL
                    INSERT INTO F_ARTICLERESSOURCE (
                    [AR_Ref],[RP_Code],[cbProt]
                            ,[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                    SELECT dest.[AR_Ref],dest.[RP_Code],[cbProt]
                            ,[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                    FROM F_ARTICLERESSOURCE_DEST dest
                    LEFT JOIN (SELECT AR_Ref,RP_Code FROM F_ARTICLERESSOURCE) src
                        ON dest.AR_Ref = src.AR_Ref
                        AND dest.RP_Code = src.RP_Code
                    WHERE src.AR_Ref IS NULL;
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
                   'F_ARTICLERESSOURCE',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
    }

    public static void sendDataElement(Connection sqlCon, String path,int unibase)
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
                updateTableDest(sqlCon, "RP_Code,AR_Ref", "'RP_Code'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteArticleRessource(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"RP_Code,AR_Ref");//initTable(sqlCon);
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArticleRessource(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_ARTICLERESSOURCE
                         WHERE EXISTS (SELECT 1 FROM F_ARTICLERESSOURCE_SUPPR WHERE F_ARTICLERESSOURCE_SUPPR.AR_Ref = F_ARTICLERESSOURCE.AR_Ref AND F_ARTICLERESSOURCE_SUPPR.RP_Code = F_ARTICLERESSOURCE.RP_Code)

                         IF OBJECT_ID('F_ARTICLERESSOURCE_SUPPR') IS NOT NULL
                         DROP TABLE F_ARTICLERESSOURCE_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
