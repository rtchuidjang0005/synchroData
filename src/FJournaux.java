import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class FJournaux extends Table {

    public static String file = "fjournaux_";
    public static String tableName = "F_JOURNAUX";
    public static String configList = "listFJournaux";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY
                SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_JOURNAUX_DEST') IS NOT NULL
                INSERT INTO F_JOURNAUX (
                [JO_Num],[JO_Intitule],[CG_Num],[JO_Type]
                      ,[JO_NumPiece],[JO_Contrepartie],[JO_SaisAnal],[JO_NotCalcTot]
                      ,[JO_Rappro],[JO_Sommeil],[JO_IFRS],[JO_Reglement]
                      ,[JO_SuiviTreso],[cbProt],[cbCreateur],[cbModification]
                      ,[cbReplication],[cbFlag])

                SELECT dest.[JO_Num],[JO_Intitule],[CG_Num],[JO_Type]
                      ,[JO_NumPiece],[JO_Contrepartie],[JO_SaisAnal],[JO_NotCalcTot]
                      ,[JO_Rappro],[JO_Sommeil],[JO_IFRS],[JO_Reglement]
                      ,[JO_SuiviTreso],[cbProt],[cbCreateur],[cbModification]
                      ,[cbReplication],[cbFlag]
                FROM F_JOURNAUX_DEST dest
                LEFT JOIN (SELECT JO_Num FROM F_JOURNAUX) src
                    ON    dest.JO_Num = src.JO_Num
                WHERE src.JO_Num IS NULL

                IF OBJECT_ID('F_JOURNAUX_DEST') IS NOT NULL
                DROP TABLE F_JOURNAUX_DEST
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
                   'F_COMPTET',
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
                updateTableDest(sqlCon, "JO_Num,JO_Type", "'JO_Num','JO_Type','CG_Num','JO_IFRS'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteJournaux(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"JO_Num,JO_Type");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteJournaux(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_JOURNAUX
                         WHERE JO_Num IN(SELECT JO_Num FROM F_JOURNAUX_SUPPR)

                         IF OBJECT_ID('F_JOURNAUX_SUPPR') IS NOT NULL
                         DROP TABLE F_JOURNAUX_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
