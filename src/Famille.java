import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Famille extends Table {

    public static String file ="famille_";
    public static String tableName = "F_FAMILLE";
    public static String configList = "listFamille";

    public static String insert(String filename)
    {
        return """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_FAMILLE_DEST') IS NOT NULL
                INSERT INTO [dbo].[F_FAMILLE]
                    ([FA_CodeFamille],[FA_Type],[FA_Intitule],[FA_UniteVen],[FA_Coef],[FA_SuiviStock],[FA_Garantie],[FA_Central]
                    ,[FA_Stat01],[FA_Stat02],[FA_Stat03],[FA_Stat04],[FA_Stat05],[FA_CodeFiscal],[FA_Pays],[FA_UnitePoids]
                    ,[FA_Escompte],[FA_Delai],[FA_HorsStat],[FA_VteDebit],[FA_NotImp],[FA_Frais01FR_Denomination]
                    ,[FA_Frais01FR_Rem01REM_Valeur],[FA_Frais01FR_Rem01REM_Type],[FA_Frais01FR_Rem02REM_Valeur],[FA_Frais01FR_Rem02REM_Type]
                    ,[FA_Frais01FR_Rem03REM_Valeur],[FA_Frais01FR_Rem03REM_Type],[FA_Frais02FR_Denomination],[FA_Frais02FR_Rem01REM_Valeur]
                    ,[FA_Frais02FR_Rem01REM_Type],[FA_Frais02FR_Rem02REM_Valeur],[FA_Frais02FR_Rem02REM_Type],[FA_Frais02FR_Rem03REM_Valeur]
                    ,[FA_Frais02FR_Rem03REM_Type],[FA_Frais03FR_Denomination],[FA_Frais03FR_Rem01REM_Valeur],[FA_Frais03FR_Rem01REM_Type]
                    ,[FA_Frais03FR_Rem02REM_Valeur],[FA_Frais03FR_Rem02REM_Type],[FA_Frais03FR_Rem03REM_Valeur],[FA_Frais03FR_Rem03REM_Type]
                    ,[FA_Contremarque],[FA_FactPoids],[FA_FactForfait],[FA_Publie],[FA_RacineRef],[FA_RacineCB],[CL_No1],[CL_No2],[CL_No3],[CL_No4]
                    ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                SELECT dest.[FA_CodeFamille],[FA_Type],[FA_Intitule],[FA_UniteVen],[FA_Coef],[FA_SuiviStock],[FA_Garantie],[FA_Central]
                           ,[FA_Stat01],[FA_Stat02],[FA_Stat03],[FA_Stat04],[FA_Stat05],[FA_CodeFiscal],[FA_Pays],[FA_UnitePoids]
                           ,[FA_Escompte],[FA_Delai],[FA_HorsStat],[FA_VteDebit],[FA_NotImp],[FA_Frais01FR_Denomination]
                           ,[FA_Frais01FR_Rem01REM_Valeur],[FA_Frais01FR_Rem01REM_Type],[FA_Frais01FR_Rem02REM_Valeur],[FA_Frais01FR_Rem02REM_Type]
                           ,[FA_Frais01FR_Rem03REM_Valeur],[FA_Frais01FR_Rem03REM_Type],[FA_Frais02FR_Denomination],[FA_Frais02FR_Rem01REM_Valeur]
                           ,[FA_Frais02FR_Rem01REM_Type],[FA_Frais02FR_Rem02REM_Valeur],[FA_Frais02FR_Rem02REM_Type],[FA_Frais02FR_Rem03REM_Valeur]
                           ,[FA_Frais02FR_Rem03REM_Type],[FA_Frais03FR_Denomination],[FA_Frais03FR_Rem01REM_Valeur],[FA_Frais03FR_Rem01REM_Type]
                           ,[FA_Frais03FR_Rem02REM_Valeur],[FA_Frais03FR_Rem02REM_Type],[FA_Frais03FR_Rem03REM_Valeur],[FA_Frais03FR_Rem03REM_Type]
                           ,[FA_Contremarque],[FA_FactPoids],[FA_FactForfait],[FA_Publie],[FA_RacineRef],[FA_RacineCB],[CL_No1],[CL_No2],[CL_No3],[CL_No4]
                           ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                FROM F_FAMILLE_DEST dest
                LEFT JOIN (SELECT [FA_CodeFamille] FROM F_FAMILLE) src
                ON dest.[FA_CodeFamille] = src.[FA_CodeFamille]
                WHERE src.[FA_CodeFamille] IS NULL ;
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
                   'F_FAMILLE',
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
                updateTableDest(sqlCon, "FA_CodeFamille", "'FA_CodeFamille','FA_Type'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                FamCompta.sendDataElement(sqlCon, path,unibase);
                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteFamille(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"FA_CodeFamille");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);

        FamCompta.getDataElement(sqlCon, path,database, time);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);


    }
    public static void deleteFamille(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_FAMILLE
                         WHERE EXISTS (SELECT 1 FROM F_FAMILLE_SUPPR WHERE F_FAMILLE_SUPPR.FA_CodeFamille = F_FAMILLE.FA_CodeFamille)

                         IF OBJECT_ID('F_FAMILLE_SUPPR') IS NOT NULL
                         DROP TABLE F_FAMILLE_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
