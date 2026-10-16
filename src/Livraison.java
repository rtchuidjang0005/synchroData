import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Livraison extends Table {

    public static String file ="livraison_";
    public static String tableName = "F_LIVRAISON";
    public static String configList = "listLivraison";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_LIVRAISON_DEST') IS NOT NULL
                INSERT INTO F_LIVRAISON ( [LI_No],[CT_Num],[LI_Intitule],[LI_Adresse],[LI_Complement],[LI_CodePostal],[LI_Ville]
                ,[LI_CodeRegion],[LI_Pays],[LI_Contact],[N_Expedition],[N_Condition],[LI_Principal]
                ,[LI_Telephone],[LI_Telecopie],[LI_EMail],[cbProt],[cbCreateur]
                ,[cbModification],[cbReplication],[cbFlag],cbMarqSource,DataBaseSource,LI_NoSource)

                SELECT (SELECT MAX(LI_No) FROM F_LIVRAISON) + ROW_NUMBER() OVER(ORDER BY dest.LI_No),dest.[CT_Num],[LI_Intitule],[LI_Adresse],[LI_Complement],[LI_CodePostal],[LI_Ville]
                ,[LI_CodeRegion],[LI_Pays],[LI_Contact],[N_Expedition],[N_Condition],[LI_Principal]
                ,[LI_Telephone],[LI_Telecopie],[LI_EMail],[cbProt],[cbCreateur]
                ,[cbModification],[cbReplication],[cbFlag],dest.[cbMarqSource],dest.[dataBaseSource],dest.LI_No
                FROM F_LIVRAISON_DEST dest
                LEFT JOIN (SELECT [LI_NoSource],[CT_Num],[DatabaseSource] FROM F_LIVRAISON) src
                ON ISNULL(dest.[LI_No],0) = ISNULL(src.[LI_NoSource],0)
                AND dest.[CT_Num] = src.[CT_Num]
                AND ISNULL(dest.[DatabaseSource],'') = ISNULL(src.[DatabaseSource],'')
                WHERE src.[LI_NoSource] IS NULL ; END TRY
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
                   'F_LIVRAISON',
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
                updateTableDest(sqlCon, "", "'LI_NoSource','LI_No','CT_Num','DataBaseSource'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteLivraison(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"LI_No,CT_Num");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }
    public static void deleteLivraison(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_LIVRAISON
                         WHERE EXISTS (SELECT 1 FROM F_LIVRAISON_SUPPR WHERE F_LIVRAISON_SUPPR.LI_No = F_LIVRAISON.LI_No AND F_LIVRAISON_SUPPR.CT_Num = F_LIVRAISON.CT_Num)

                         IF OBJECT_ID('F_LIVRAISON_SUPPR') IS NOT NULL
                         DROP TABLE F_LIVRAISON_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
