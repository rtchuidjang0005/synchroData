import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class ArtFourniss extends Table {

    public static String file ="ArtFourniss_";
    public static String tableName = "F_ARTFOURNISS";
    public static String configList = "listArtFourniss";

    public static String insert(String filename)
    {
        return
                        """
                                  BEGIN TRY  SET DATEFORMAT ymd;
                                 IF OBJECT_ID('F_ARTFOURNISS_DEST') IS NOT NULL
                                  INSERT INTO [dbo].[F_ARTFOURNISS]
                                  ([AR_Ref],[CT_Num],[AF_RefFourniss],[AF_PrixAch],[AF_Unite],[AF_Conversion],[AF_DelaiAppro]
                                      ,[AF_Garantie],[AF_Colisage],[AF_QteMini],[AF_QteMont],[EG_Champ],[AF_Principal]
                                      ,[AF_PrixDev],[AF_Devise],[AF_Remise],[AF_ConvDiv],[AF_TypeRem],[AF_CodeBarre]
                                      ,[AF_PrixAchNouv],[AF_PrixDevNouv],[AF_RemiseNouv],[AF_DateApplication],[cbProt],[cbCreateur]
                                      ,[cbModification],[cbReplication],[cbFlag])

                                  SELECT dest.[AR_Ref],dest.[CT_Num],[AF_RefFourniss],[AF_PrixAch],[AF_Unite],[AF_Conversion],[AF_DelaiAppro]
                                      ,[AF_Garantie],[AF_Colisage],[AF_QteMini],[AF_QteMont],[EG_Champ],[AF_Principal]
                                      ,[AF_PrixDev],[AF_Devise],[AF_Remise],[AF_ConvDiv],[AF_TypeRem],[AF_CodeBarre]
                                      ,[AF_PrixAchNouv],[AF_PrixDevNouv],[AF_RemiseNouv],[AF_DateApplication],[cbProt],[cbCreateur]
                                      ,[cbModification],[cbReplication],[cbFlag]
                                  FROM F_ARTFOURNISS_DEST dest
                                  LEFT JOIN (SELECT [AR_Ref],[CT_Num] FROM F_ARTFOURNISS) src
                                  ON dest.[AR_Ref] = src.[AR_Ref]
                                  AND dest.[CT_Num] = src.[CT_Num]
                                  WHERE src.[AR_Ref] IS NULL ;
                                  IF OBJECT_ID('F_ARTFOURNISS_DEST') IS NOT NULL
                                  DROP TABLE F_ARTFOURNISS_DEST
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
                                   'F_ARTFOURNISS',
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
                updateTableDest(sqlCon, "AR_Ref,CT_Num", "'AR_Ref','CT_Num'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteArtFourniss(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref,CT_Num");//initTable(sqlCon);
        getData(sqlCon, selectSourceTable(tableName,database,true)/*list()*/, tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArtFourniss(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_ARTFOURNISS
                         WHERE EXISTS (SELECT 1 FROM F_ARTFOURNISS_SUPPR WHERE F_ARTFOURNISS_SUPPR.AR_Ref = F_ARTFOURNISS.AR_Ref AND F_ARTFOURNISS_SUPPR.CT_Num = F_ARTFOURNISS.CT_Num   )

                         IF OBJECT_ID('F_ARTFOURNISS_SUPPR') IS NOT NULL
                         DROP TABLE F_ARTFOURNISS_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
