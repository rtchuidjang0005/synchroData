import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class ArtCompta extends Table {
    public static String file ="ArtCompta_";
    public static String tableName = "F_ARTCOMPTA";
    public static String configList = "listArtCompta";

    public static String insert(String filename)
    {
        return
                """
                         BEGIN TRY  SET DATEFORMAT ymd;

                         IF OBJECT_ID('F_ARTCOMPTA_DEST') IS NOT NULL
                         INSERT INTO [dbo].[F_ARTCOMPTA]
                             ([AR_Ref],[ACP_Type],[ACP_Champ],[ACP_ComptaCPT_CompteG]
                              ,[ACP_ComptaCPT_CompteA],[ACP_ComptaCPT_Taxe1],[ACP_ComptaCPT_Taxe2],[ACP_ComptaCPT_Taxe3]
                              ,[ACP_TypeFacture],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                         SELECT dest.[AR_Ref],dest.[ACP_Type],dest.[ACP_Champ],[ACP_ComptaCPT_CompteG]
                              ,[ACP_ComptaCPT_CompteA],[ACP_ComptaCPT_Taxe1],[ACP_ComptaCPT_Taxe2],[ACP_ComptaCPT_Taxe3]
                              ,[ACP_TypeFacture],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                         FROM F_ARTCOMPTA_DEST dest
                         LEFT JOIN (SELECT [AR_Ref],[ACP_Type],[ACP_Champ] FROM F_ARTCOMPTA) src
                         ON dest.[AR_Ref] = src.[AR_Ref]
                         AND     dest.ACP_Type = src.ACP_Type
                         AND     dest.ACP_Champ = src.ACP_Champ
                         WHERE src.[AR_Ref] IS NULL ;
                         IF OBJECT_ID('F_ARTCOMPTA_DEST') IS NOT NULL
                         DROP TABLE F_ARTCOMPTA_DEST;
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
                           'F_ARTCOMPTA',
                           GETDATE());
                        END CATCH\
                        """.formatted(filename);
    }

    public static void sendDataElement(Connection sqlCon, String path,int unibase) {
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file);
        String[] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                readOnFile(path, filename, tableName + "_DEST", sqlCon);

                readOnFile(path, "deleteList" + filename, tableName + "_SUPPR", sqlCon);
                updateTableDest(sqlCon, "AR_Ref,ACP_Type,ACP_Champ", "'AR_Ref','ACP_Type','ACP_Champ'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteArtCompta(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref,ACP_Type,ACP_Champ");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArtCompta(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_ARTCOMPTA
                         WHERE EXISTS (SELECT 1 FROM F_ARTCOMPTA_SUPPR WHERE F_ARTCOMPTA_SUPPR.AR_Ref = F_ARTCOMPTA.AR_Ref   AND F_ARTCOMPTA_SUPPR.ACP_Type = F_ARTCOMPTA.ACP_Type AND F_ARTCOMPTA_SUPPR.ACP_Champ = F_ARTCOMPTA.ACP_Champ)

                         IF OBJECT_ID('F_ARTCOMPTA_SUPPR') IS NOT NULL
                         DROP TABLE F_ARTCOMPTA_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
