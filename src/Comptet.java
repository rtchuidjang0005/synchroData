import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Comptet extends Table {

    public static String file = "comptet_";
    public static String tableName = "F_COMPTET";
    public static String configList = "listComptet";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY
                SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_COMPTET_DEST') IS NOT NULL
                INSERT INTO F_COMPTET (
                [CT_Num],[CT_Intitule],[CT_Type],[CG_NumPrinc],[CT_Qualite],[CT_Classement]
                      ,[CT_Contact],[CT_Adresse],[CT_Complement],[CT_CodePostal]
                      ,[CT_Ville],[CT_CodeRegion],[CT_Pays],[CT_Raccourci]
                      ,[BT_Num],[N_Devise],[CT_Ape],[CT_Identifiant]
                      ,[CT_Siret],[CT_Statistique01],[CT_Statistique02],[CT_Statistique03]
                      ,[CT_Statistique04],[CT_Statistique05],[CT_Statistique06],[CT_Statistique07]
                      ,[CT_Statistique08],[CT_Statistique09],[CT_Statistique10],[CT_Commentaire]
                      ,[CT_Encours],[CT_Assurance],[CT_NumPayeur],[N_Risque]
                      ,[CO_No],[N_CatTarif],[CT_Taux01],[CT_Taux02],[CT_Taux03],[CT_Taux04]
                      ,[N_CatCompta],[N_Period],[CT_Facture],[CT_BLFact]
                      ,[CT_Langue],[CT_Edi1],[CT_Edi2],[CT_Edi3],[N_Expedition],[N_Condition]
                      ,[CT_DateCreate],[CT_Saut],[CT_Lettrage],[CT_ValidEch]
                      ,[CT_Sommeil],[DE_No],[CT_ControlEnc],[CT_NotRappel],[N_Analytique],[CA_Num],[CT_Telephone],[CT_Telecopie]
                      ,[CT_EMail],[CT_Site],[CT_Coface],[CT_Surveillance],[CT_SvDateCreate],[CT_SvFormeJuri]
                      ,[CT_SvEffectif],[CT_SvCA],[CT_SvResultat],[CT_SvIncident],[CT_SvDateIncid],[CT_SvPrivil]
                      ,[CT_SvRegul],[CT_SvCotation],[CT_SvDateMaj],[CT_SvObjetMaj]
                      ,[CT_SvDateBilan],[CT_SvNbMoisBilan],[N_AnalytiqueIFRS],[CA_NumIFRS]
                      ,[CT_PrioriteLivr],[CT_LivrPartielle]
                      ,[MR_No],[CT_NotPenal],[EB_No],[CT_NumCentrale],[CT_DateFermeDebut],[CT_DateFermeFin]
                      ,[CT_FactureElec],[CT_TypeNIF],[CT_RepresentInt],[CT_RepresentNIF]
                      ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                SELECT dest.[CT_Num],[CT_Intitule],[CT_Type],[CG_NumPrinc],LEFT([CT_Qualite],17),[CT_Classement]
                      ,[CT_Contact],[CT_Adresse],[CT_Complement],[CT_CodePostal]
                      ,[CT_Ville],[CT_CodeRegion],[CT_Pays],[CT_Raccourci]
                      /*,[BT_Num]*/,0,[N_Devise],[CT_Ape],[CT_Identifiant]
                      ,[CT_Siret],[CT_Statistique01],[CT_Statistique02],[CT_Statistique03]
                      ,[CT_Statistique04],[CT_Statistique05],[CT_Statistique06],[CT_Statistique07]
                      ,[CT_Statistique08],[CT_Statistique09],[CT_Statistique10],[CT_Commentaire]
                      ,[CT_Encours],[CT_Assurance],srcP.[CT_Num],[N_Risque]
                      ,[CO_No],[N_CatTarif],[CT_Taux01],[CT_Taux02],[CT_Taux03],[CT_Taux04]
                      ,[N_CatCompta],[N_Period],[CT_Facture],[CT_BLFact]
                      ,[CT_Langue],[CT_Edi1],[CT_Edi2],[CT_Edi3],[N_Expedition],[N_Condition]
                      ,[CT_DateCreate],[CT_Saut],[CT_Lettrage],[CT_ValidEch]
                      ,[CT_Sommeil],[DE_No],[CT_ControlEnc],[CT_NotRappel],[N_Analytique],[CA_Num],[CT_Telephone],[CT_Telecopie]
                      ,[CT_EMail],[CT_Site],[CT_Coface],[CT_Surveillance],[CT_SvDateCreate],[CT_SvFormeJuri]
                      ,[CT_SvEffectif],[CT_SvCA],[CT_SvResultat],[CT_SvIncident],[CT_SvDateIncid],[CT_SvPrivil]
                      ,[CT_SvRegul],[CT_SvCotation],[CT_SvDateMaj],[CT_SvObjetMaj]
                      ,[CT_SvDateBilan],[CT_SvNbMoisBilan],[N_AnalytiqueIFRS],[CA_NumIFRS]
                      ,[CT_PrioriteLivr],[CT_LivrPartielle]
                      ,[MR_No],[CT_NotPenal],[EB_No],srcC.[CT_Num],[CT_DateFermeDebut],[CT_DateFermeFin]
                      ,[CT_FactureElec],[CT_TypeNIF],[CT_RepresentInt],[CT_RepresentNIF]
                      ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                FROM F_COMPTET_DEST dest
                LEFT JOIN (SELECT CT_Num FROM F_COMPTET) src
                    ON    dest.CT_Num = src.CT_Num
                LEFT JOIN (SELECT CT_Num FROM F_COMPTET) srcP
                    ON    dest.CT_NumPayeur = srcP.CT_Num
                LEFT JOIN (SELECT CT_Num FROM F_COMPTET) srcC
                    ON    dest.CT_NumCentrale = srcC.CT_Num
                WHERE src.CT_Num IS NULL

                IF OBJECT_ID('F_COMPTET_DEST') IS NOT NULL
                DROP TABLE F_COMPTET_DEST
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

                disableTrigger(sqlCon,tableName);
                updateTableDest(sqlCon, "CT_Num,CT_Type", "'CT_Num','CT_Type','DE_No'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteComptet(sqlCon, path, filename);
                enableTrigger(sqlCon,tableName);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"CT_Num,CT_Type");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteComptet(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_COMPTET
                         WHERE CT_Num IN(SELECT CT_Num FROM F_COMPTET_SUPPR)

                         IF OBJECT_ID('F_COMPTET_SUPPR') IS NOT NULL
                         DROP TABLE F_COMPTET_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
