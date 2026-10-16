import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Compteg extends Table {

    public static String file = "compteg_";
    public static String tableName = "F_COMPTEG";
    public static String configList = "listCompteg";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY
                SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_COMPTEG_DEST') IS NOT NULL
                INSERT INTO [dbo].[F_COMPTEG]
                           ([CG_Num],[CG_Type],[CG_Intitule],[CG_Classement]
                           ,[N_Nature],[CG_Report],[CR_Num],[CG_Raccourci]
                           ,[CG_Saut],[CG_Regroup],[CG_Analytique],[CG_Echeance]
                           ,[CG_Quantite],[CG_Lettrage],[CG_Tiers],[CG_DateCreate]
                           ,[CG_Devise],[N_Devise],[TA_Code],[CG_Sommeil]
                           ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])
                     SELECT dest.[CG_Num],[CG_Type],[CG_Intitule],[CG_Classement]
                           ,[N_Nature],[CG_Report],[CR_Num],[CG_Raccourci]
                           ,[CG_Saut],[CG_Regroup],[CG_Analytique],[CG_Echeance]
                           ,[CG_Quantite],[CG_Lettrage],[CG_Tiers],[CG_DateCreate]
                           ,[CG_Devise],[N_Devise],[TA_Code],[CG_Sommeil]
                           ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                FROM    [F_COMPTEG_DEST] dest
                LEFT JOIN (SELECT CG_Num FROM F_COMPTEG) src
                    ON    dest.CG_Num = src.CG_Num
                WHERE    src.CG_Num IS NULL
                IF OBJECT_ID('F_COMPTEG_DEST') IS NOT NULL
                DROP TABLE F_COMPTEG_DEST
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
                   'F_COMPTEG',
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
                updateTableDest(sqlCon, "CG_Num,CG_Type", "'CG_Num','CG_Type'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteCompteg(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"CG_Num");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteCompteg(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_COMPTEG
                         WHERE CG_Num IN(SELECT CG_Num FROM F_COMPTEG_SUPPR)

                         IF OBJECT_ID('F_COMPTEG_SUPPR') IS NOT NULL
                         DROP TABLE F_COMPTEG_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
