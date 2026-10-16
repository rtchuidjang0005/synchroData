import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class ArtClient extends Table{
    public static String file ="ArtClient_";
    public static String tableName = "F_ARTCLIENT";
    public static String configList = "listArtClient";

    public static String insert(String filename)
    {
        return
                        """
                                 BEGIN TRY SET DATEFORMAT ymd;
                                 IF OBJECT_ID('F_ARTCLIENT_DEST') IS NOT NULL
                                 INSERT INTO [dbo].[F_ARTCLIENT]
                                 ([AR_Ref],[AC_Categorie],[AC_PrixVen],[AC_Coef]
                                      ,[AC_PrixTTC],[AC_Arrondi],[AC_QteMont],[EG_Champ]
                                      ,[AC_PrixDev],[AC_Devise],[CT_Num],[AC_Remise]
                                      ,[AC_Calcul],[AC_TypeRem],[AC_RefClient],[AC_CoefNouv]
                                      ,[AC_PrixVenNouv],[AC_PrixDevNouv],[AC_RemiseNouv],[AC_DateApplication]
                                      ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                                 SELECT dest.[AR_Ref],dest.[AC_Categorie],[AC_PrixVen],[AC_Coef]
                                      ,[AC_PrixTTC],[AC_Arrondi],[AC_QteMont],[EG_Champ]
                                      ,[AC_PrixDev],[AC_Devise],[CT_Num],[AC_Remise]
                                      ,[AC_Calcul],[AC_TypeRem],[AC_RefClient],[AC_CoefNouv]
                                      ,[AC_PrixVenNouv],[AC_PrixDevNouv],[AC_RemiseNouv],[AC_DateApplication]
                                      ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                                 FROM F_ARTCLIENT_DEST dest
                                 LEFT JOIN (SELECT [AR_Ref],[AC_Categorie] FROM F_ARTCLIENT) src
                                 ON dest.[AR_Ref] = src.[AR_Ref]
                                 AND dest.[AC_Categorie] = src.[AC_Categorie]
                                 WHERE src.[AR_Ref] IS NULL ;
                                 IF OBJECT_ID('F_ARTCLIENT_DEST') IS NOT NULL
                                 DROP TABLE F_ARTCLIENT_DEST;
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
                                   'Insert '+' %s',
                                   'F_ARTCLIENT',
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
                updateTableDest(sqlCon, "AR_Ref,AC_Categorie", "'AR_Ref','AC_Categorie','CT_Num'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteArtClient(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref,AC_Categorie");//initTable(sqlCon);
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArtClient(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_ARTCLIENT
                         WHERE EXISTS (SELECT 1 FROM F_ARTCLIENT_SUPPR WHERE F_ARTCLIENT_SUPPR.AR_Ref = F_ARTCLIENT.AR_Ref   AND F_ARTCLIENT_SUPPR.AC_Categorie = F_ARTCLIENT.AC_Categorie )

                         IF OBJECT_ID('F_ARTCLIENT_SUPPR') IS NOT NULL
                         DROP TABLE F_ARTCLIENT_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
