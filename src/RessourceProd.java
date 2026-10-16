import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class RessourceProd extends Table {

    public static String file ="ressourceprod_";
    public static String tableName = "F_RESSOURCEPROD";
    public static String configList = "listRessourceProd";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_RESSOURCEPROD_DEST') IS NOT NULL
                    INSERT INTO F_RESSOURCEPROD (
                    [RP_Code],[RP_Type],[RP_Intitule],[RP_Complement],[RP_Central]
                      ,[RP_Visite],[RP_CoutStd],[RP_Temps],[RP_Sommeil]
                      ,[RP_Capacite],[RP_Commentaire],[RP_DateCreation],[RP_TypeRess]
                      ,[RP_CodeExterne],[DE_No],[RP_Adresse],[RP_ComplementAdresse],[RP_CodePostal]
                      ,[RP_Ville],[RP_Region],[RP_Pays],[RP_Telephone],[RP_Portable]
                      ,[RP_EMail],[AR_RefDefaut],[RP_Unite],[RP_Continue],[CAL_No]
                      ,[RP_Horaire0101RP_PlageDebut],[RP_Horaire0101RP_PlageFin],[RP_Horaire0102RP_PlageDebut]
                      ,[RP_Horaire0102RP_PlageFin],[RP_Horaire0201RP_PlageDebut],[RP_Horaire0201RP_PlageFin],[RP_Horaire0202RP_PlageDebut]
                      ,[RP_Horaire0202RP_PlageFin],[RP_Horaire0301RP_PlageDebut],[RP_Horaire0301RP_PlageFin]
                      ,[RP_Horaire0302RP_PlageDebut],[RP_Horaire0302RP_PlageFin],[RP_Horaire0401RP_PlageDebut]
                      ,[RP_Horaire0401RP_PlageFin],[RP_Horaire0402RP_PlageDebut],[RP_Horaire0402RP_PlageFin]
                      ,[RP_Horaire0501RP_PlageDebut],[RP_Horaire0501RP_PlageFin],[RP_Horaire0502RP_PlageDebut]
                      ,[RP_Horaire0502RP_PlageFin],[RP_Horaire0601RP_PlageDebut],[RP_Horaire0601RP_PlageFin]
                      ,[RP_Horaire0602RP_PlageDebut],[RP_Horaire0602RP_PlageFin],[RP_Horaire0701RP_PlageDebut]
                      ,[RP_Horaire0701RP_PlageFin],[RP_Horaire0702RP_PlageDebut],[RP_Horaire0702RP_PlageFin]
                      ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                    SELECT dest.[RP_Code],[RP_Type],[RP_Intitule],[RP_Complement],[RP_Central]
                      ,[RP_Visite],[RP_CoutStd],[RP_Temps],[RP_Sommeil]
                      ,[RP_Capacite],[RP_Commentaire],[RP_DateCreation],[RP_TypeRess]
                      ,[RP_CodeExterne],[DE_No],[RP_Adresse],[RP_ComplementAdresse],[RP_CodePostal]
                      ,[RP_Ville],[RP_Region],[RP_Pays],[RP_Telephone],[RP_Portable]
                      ,[RP_EMail],null/*[AR_RefDefaut]*/,[RP_Unite],[RP_Continue],[CAL_No]
                      ,[RP_Horaire0101RP_PlageDebut],[RP_Horaire0101RP_PlageFin],[RP_Horaire0102RP_PlageDebut]
                      ,[RP_Horaire0102RP_PlageFin],[RP_Horaire0201RP_PlageDebut],[RP_Horaire0201RP_PlageFin],[RP_Horaire0202RP_PlageDebut]
                      ,[RP_Horaire0202RP_PlageFin],[RP_Horaire0301RP_PlageDebut],[RP_Horaire0301RP_PlageFin]
                      ,[RP_Horaire0302RP_PlageDebut],[RP_Horaire0302RP_PlageFin],[RP_Horaire0401RP_PlageDebut]
                      ,[RP_Horaire0401RP_PlageFin],[RP_Horaire0402RP_PlageDebut],[RP_Horaire0402RP_PlageFin]
                      ,[RP_Horaire0501RP_PlageDebut],[RP_Horaire0501RP_PlageFin],[RP_Horaire0502RP_PlageDebut]
                      ,[RP_Horaire0502RP_PlageFin],[RP_Horaire0601RP_PlageDebut],[RP_Horaire0601RP_PlageFin]
                      ,[RP_Horaire0602RP_PlageDebut],[RP_Horaire0602RP_PlageFin],[RP_Horaire0701RP_PlageDebut]
                      ,[RP_Horaire0701RP_PlageFin],[RP_Horaire0702RP_PlageDebut],[RP_Horaire0702RP_PlageFin]
                      ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                     FROM F_RESSOURCEPROD_DEST dest
                    LEFT JOIN (SELECT RP_Code FROM F_RESSOURCEPROD) src
                        ON dest.RP_Code = src.RP_Code
                    WHERE src.RP_Code IS NULL
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
                   'F_RESSOURCEPROD',
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
                updateTableDest(sqlCon, "RP_Code,RP_Type", "'RP_Code','RP_Type','RP_TypeRess'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteRessourceProd(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"RP_Code,RP_Type");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteRessourceProd(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_RESSOURCEPROD
                         WHERE EXISTS (SELECT 1 FROM F_RESSOURCEPROD_SUPPR WHERE F_RESSOURCEPROD_SUPPR.RP_Type = F_RESSOURCEPROD.RP_Type AND F_RESSOURCEPROD_SUPPR.RP_Code = F_RESSOURCEPROD.RP_Code)

                         IF OBJECT_ID('F_RESSOURCEPROD_SUPPR') IS NOT NULL
                         DROP TABLE F_RESSOURCEPROD_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
