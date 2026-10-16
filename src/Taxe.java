import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Taxe extends Table {
    public static String file ="taxe_";
    public static String tableName = "F_TAXE";
    public static String configList = "listTaxe";

    public static String insert(String filename)
    {
        return
                """
                         BEGIN TRY  SET DATEFORMAT ymd;

                         IF OBJECT_ID('F_TAXE_DEST') IS NOT NULL
                         INSERT INTO [dbo].[F_TAXE]
                                   ([TA_Intitule],[TA_TTaux],[TA_Taux],[TA_Type],[CG_Num],[TA_No],[TA_Code],[TA_NP]
                                   ,[TA_Sens],[TA_Provenance],[TA_Regroup],[TA_Assujet],[TA_GrilleBase],[TA_GrilleTaxe]
                                   ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                                   SELECT [TA_Intitule],[TA_TTaux],[TA_Taux],[TA_Type],[CG_Num],[TA_No],dest.[TA_Code],[TA_NP]
                                   ,[TA_Sens],[TA_Provenance],[TA_Regroup],[TA_Assujet],[TA_GrilleBase],[TA_GrilleTaxe]
                                   ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                                   FROM F_TAXE_DEST dest
                                   LEFT JOIN (SELECT TA_Code FROM F_TAXE) src
                                   ON    dest.TA_Code = src.TA_Code
                                   WHERE src.TA_Code IS NULL;

                                   IF OBJECT_ID('F_TAXE_DEST') IS NOT NULL
                                   DROP TABLE F_TAXE_DEST; END TRY
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
                           'F_TAXE',
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
                updateTableDest(sqlCon, "TA_Code", "'TA_Code'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteArtCompta(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"TA_Code");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArtCompta(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE src
                         FROM F_TAXE src
                         INNER JOIN F_TAXE_SUPPR del
                         ON src.TA_Code = del.TA_Code;
                         IF OBJECT_ID('F_TAXE_SUPPR') IS NOT NULL
                         DROP TABLE F_TAXE_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
