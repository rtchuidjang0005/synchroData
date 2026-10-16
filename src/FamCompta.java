import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class FamCompta extends Table {

    public static String file ="famCompta_";
    public static String tableName = "F_FAMCOMPTA";
    public static String configList = "listFamCompta";

    public static String insert(String filename)
    {
        return          """
                 BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_FAMCOMPTA_DEST') IS NOT NULL
                 INSERT INTO [dbo].[F_FAMCOMPTA]
                     ([FA_CodeFamille],[FCP_Type],[FCP_Champ],[FCP_ComptaCPT_CompteG]
                      ,[FCP_ComptaCPT_CompteA],[FCP_ComptaCPT_Taxe1],[FCP_ComptaCPT_Taxe2],[FCP_ComptaCPT_Taxe3]
                      ,[FCP_TypeFacture],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                 SELECT dest.[FA_CodeFamille],dest.[FCP_Type],dest.[FCP_Champ],[FCP_ComptaCPT_CompteG]
                      ,[FCP_ComptaCPT_CompteA],[FCP_ComptaCPT_Taxe1],[FCP_ComptaCPT_Taxe2],[FCP_ComptaCPT_Taxe3]
                      ,[FCP_TypeFacture],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                 FROM F_FAMCOMPTA_DEST dest
                 LEFT JOIN (SELECT [FA_CodeFamille],[FCP_Type],[FCP_Champ] FROM F_FAMCOMPTA) src
                 ON dest.[FA_CodeFamille] = src.[FA_CodeFamille]
                 AND     dest.FCP_Type = src.FCP_Type
                 AND     dest.FCP_Champ = src.FCP_Champ
                 WHERE src.[FA_CodeFamille] IS NULL ;
                 IF OBJECT_ID('F_FAMCOMPTA_DEST') IS NOT NULL
                 DROP TABLE F_FAMCOMPTA_DEST; END TRY
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
                   'F_FAMCOMPTA',
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
                updateTableDest(sqlCon, "FA_CodeFamille,FCP_Type,FCP_Champ", "'FA_CodeFamille','FCP_Type','FCP_Champ'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));
                deleteTempTable(sqlCon, tableName + "_DEST");

                deleteFamCompta(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"FA_CodeFamille,FCP_Type,FCP_Champ");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }
    public static void deleteFamCompta(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_FAMCOMPTA
                         WHERE EXISTS (SELECT 1 FROM F_FAMCOMPTA_SUPPR WHERE F_FAMCOMPTA_SUPPR.FA_CodeFamille = F_FAMCOMPTA.FA_CodeFamille   AND F_FAMCOMPTA_SUPPR.FCP_Type = F_FAMCOMPTA.FCP_Type AND F_FAMCOMPTA_SUPPR.FCP_Champ = F_FAMCOMPTA.FCP_Champ)

                         IF OBJECT_ID('F_FAMCOMPTA_SUPPR') IS NOT NULL
                         DROP TABLE F_FAMCOMPTA_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
