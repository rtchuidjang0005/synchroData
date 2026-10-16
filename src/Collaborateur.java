import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Collaborateur extends Table {

    public static String file = "collaborateur_";
    public static String tableName = "F_COLLABORATEUR";
    public static String configList = "listCollaborateur";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_COLLABORATEUR_DEST') IS NOT NULL
                INSERT INTO F_COLLABORATEUR (
                [CO_No],[CO_Nom],[CO_Prenom],[CO_Fonction],[CO_Adresse],[CO_Complement],[CO_CodePostal],[CO_Ville],[CO_CodeRegion]
                      ,[CO_Pays],[CO_Service],[CO_Vendeur],[CO_Caissier],[CO_DateCreation],[CO_Acheteur],[CO_Telephone],[CO_Telecopie]
                      ,[CO_EMail],[CO_Receptionnaire],[PROT_No],[CO_TelPortable],[CO_ChargeRecouvr],[cbProt],[cbCreateur]
                      ,[cbModification],[cbReplication],[cbFlag])

                SELECT dest.[CO_No],[CO_Nom],[CO_Prenom],[CO_Fonction],[CO_Adresse],[CO_Complement],[CO_CodePostal],[CO_Ville],[CO_CodeRegion]
                      ,[CO_Pays],[CO_Service],[CO_Vendeur],[CO_Caissier],[CO_DateCreation],[CO_Acheteur],[CO_Telephone],[CO_Telecopie]
                      ,[CO_EMail],[CO_Receptionnaire],[PROT_No],[CO_TelPortable],[CO_ChargeRecouvr],[cbProt],[cbCreateur]
                      ,[cbModification],[cbReplication],[cbFlag]
                FROM F_COLLABORATEUR_DEST dest
                LEFT JOIN (SELECT CO_No FROM F_COLLABORATEUR) src
                    ON    dest.CO_No = src.CO_No
                WHERE src.CO_No IS NULL

                IF OBJECT_ID('F_COLLABORATEUR_DEST') IS NOT NULL
                DROP TABLE F_COLLABORATEUR_DEST;
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
                   'F_COLLABORATEUR',
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
                updateTableDest(sqlCon, "CO_No", "'CO_No','CO_Nom'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteCollaborateur(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"CO_No");//initTable(sqlCon);
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteCollaborateur(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_COLLABORATEUR
                         WHERE CO_No IN(SELECT CO_No FROM F_COLLABORATEUR_SUPPR)

                         IF OBJECT_ID('F_COLLABORATEUR_SUPPR') IS NOT NULL
                         DROP TABLE F_COLLABORATEUR_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
