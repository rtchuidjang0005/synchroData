import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Article extends Table {
    public static String file = "article_";
    public static String tableName = "F_ARTICLE";
    public static String configList = "listArticle";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_ARTICLE_DEST') IS NOT NULL
                INSERT INTO F_ARTICLE (
                [AR_Ref],[AR_Design],[FA_CodeFamille],[AR_Substitut],[AR_Raccourci]
                ,[AR_Garantie],[AR_UnitePoids],[AR_PoidsNet],[AR_PoidsBrut],[AR_UniteVen]
                ,[AR_PrixAch],[AR_Coef],[AR_PrixVen],[AR_PrixTTC],[AR_Gamme1],[AR_Gamme2]
                ,[AR_SuiviStock],[AR_Nomencl],[AR_Stat01],[AR_Stat02],[AR_Stat03],[AR_Stat04],[AR_Stat05]
                ,[AR_Escompte],[AR_Delai],[AR_HorsStat],[AR_VteDebit],[AR_NotImp],[AR_Sommeil],[AR_Langue1],[AR_Langue2],[AR_CodeEdiED_Code1]
                ,[AR_CodeEdiED_Code2],[AR_CodeEdiED_Code3],[AR_CodeEdiED_Code4],[AR_CodeBarre],[AR_CodeFiscal],[AR_Pays]
                ,[AR_Frais01FR_Denomination],[AR_Frais01FR_Rem01REM_Valeur],[AR_Frais01FR_Rem01REM_Type],[AR_Frais01FR_Rem02REM_Valeur]
                ,[AR_Frais01FR_Rem02REM_Type],[AR_Frais01FR_Rem03REM_Valeur],[AR_Frais01FR_Rem03REM_Type],[AR_Frais02FR_Denomination]
                ,[AR_Frais02FR_Rem01REM_Valeur],[AR_Frais02FR_Rem01REM_Type],[AR_Frais02FR_Rem02REM_Valeur],[AR_Frais02FR_Rem02REM_Type]
                ,[AR_Frais02FR_Rem03REM_Valeur],[AR_Frais02FR_Rem03REM_Type],[AR_Frais03FR_Denomination],[AR_Frais03FR_Rem01REM_Valeur]
                ,[AR_Frais03FR_Rem01REM_Type],[AR_Frais03FR_Rem02REM_Valeur],[AR_Frais03FR_Rem02REM_Type],[AR_Frais03FR_Rem03REM_Valeur]
                ,[AR_Frais03FR_Rem03REM_Type],[AR_Condition],[AR_PUNet],[AR_Contremarque],[AR_FactPoids],[AR_FactForfait],[AR_DateCreation],[AR_SaisieVar]
                ,[AR_Transfere],[AR_Publie],[AR_DateModif],[AR_Photo],[AR_PrixAchNouv],[AR_CoefNouv],[AR_PrixVenNouv],[AR_DateApplication]
                ,[AR_CoutStd],[AR_QteComp],[AR_QteOperatoire],[CO_No],[AR_Prevision],[CL_No1],[CL_No2],[CL_No3]
                ,[CL_No4],[AR_Type],[RP_CodeDefaut],[cbProt],[cbCreateur]
                ,[cbReplication],[cbFlag])

                SELECT dest.[AR_Ref],LEFT(dest.[AR_Design],69),dest.[FA_CodeFamille],dest.[AR_Substitut],dest.[AR_Raccourci]
                ,dest.[AR_Garantie],dest.[AR_UnitePoids],dest.[AR_PoidsNet],dest.[AR_PoidsBrut],dest.[AR_UniteVen]
                ,dest.[AR_PrixAch],dest.[AR_Coef],dest.[AR_PrixVen],dest.[AR_PrixTTC],dest.[AR_Gamme1],dest.[AR_Gamme2]
                ,dest.[AR_SuiviStock],dest.[AR_Nomencl],dest.[AR_Stat01],dest.[AR_Stat02],dest.[AR_Stat03],dest.[AR_Stat04],dest.[AR_Stat05]
                ,dest.[AR_Escompte],dest.[AR_Delai],dest.[AR_HorsStat],dest.[AR_VteDebit],dest.[AR_NotImp],dest.[AR_Sommeil],dest.cbMarqSource/*dest.[AR_Langue1]*/,dest.[AR_Langue2],dest.[AR_CodeEdiED_Code1]
                ,dest.[AR_CodeEdiED_Code2],dest.[AR_CodeEdiED_Code3],dest.[AR_CodeEdiED_Code4],dest.[AR_CodeBarre],dest.[AR_CodeFiscal],dest.[AR_Pays]
                ,dest.[AR_Frais01FR_Denomination],dest.[AR_Frais01FR_Rem01REM_Valeur],dest.[AR_Frais01FR_Rem01REM_Type],dest.[AR_Frais01FR_Rem02REM_Valeur]
                ,dest.[AR_Frais01FR_Rem02REM_Type],dest.[AR_Frais01FR_Rem03REM_Valeur],dest.[AR_Frais01FR_Rem03REM_Type],dest.[AR_Frais02FR_Denomination]
                ,dest.[AR_Frais02FR_Rem01REM_Valeur],dest.[AR_Frais02FR_Rem01REM_Type],dest.[AR_Frais02FR_Rem02REM_Valeur],dest.[AR_Frais02FR_Rem02REM_Type]
                ,dest.[AR_Frais02FR_Rem03REM_Valeur],dest.[AR_Frais02FR_Rem03REM_Type],dest.[AR_Frais03FR_Denomination],dest.[AR_Frais03FR_Rem01REM_Valeur]
                ,dest.[AR_Frais03FR_Rem01REM_Type],dest.[AR_Frais03FR_Rem02REM_Valeur],dest.[AR_Frais03FR_Rem02REM_Type],dest.[AR_Frais03FR_Rem03REM_Valeur]
                ,dest.[AR_Frais03FR_Rem03REM_Type],dest.[AR_Condition],dest.[AR_PUNet],dest.[AR_Contremarque],dest.[AR_FactPoids],dest.[AR_FactForfait],dest.[AR_DateCreation],dest.[AR_SaisieVar]
                ,dest.[AR_Transfere],dest.[AR_Publie],dest.[AR_DateModif],dest.[AR_Photo],dest.[AR_PrixAchNouv],dest.[AR_CoefNouv],dest.[AR_PrixVenNouv],dest.[AR_DateApplication]
                ,dest.[AR_CoutStd],dest.[AR_QteComp],dest.[AR_QteOperatoire],null/*dest.[CO_No]*/,dest.[AR_Prevision],dest.[CL_No1],dest.[CL_No2],dest.[CL_No3]
                ,dest.[CL_No4],dest.[AR_Type],null/*dest.[RP_CodeDefaut]*/,dest.[cbProt],dest.[cbCreateur],dest.[cbReplication],dest.[cbFlag]
                FROM F_ARTICLE_DEST dest
                LEFT JOIN F_ARTICLE src
                    ON dest.AR_Ref = src.AR_Ref
                WHERE src.AR_Ref IS NULL
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
                   'F_ARTICLE',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
    }

    public static void linkArticle(Connection sqlCon)
    {
        String query = """
                UPDATE F_ARTICLE
                     SET CO_No = F_ARTICLE_DEST.CO_No
                         , RP_CodeDefaut = F_ARTICLE_DEST.RP_CodeDefaut
                 FROM F_ARTICLE_DEST
                 WHERE F_ARTICLE_DEST.AR_Ref = F_ARTICLE.AR_Ref
                 UPDATE F_RESSOURCEPROD
                     SET AR_RefDefaut = F_RESSOURCEPROD_DEST.AR_RefDefaut
                 FROM F_RESSOURCEPROD_DEST
                 WHERE F_RESSOURCEPROD_DEST.RP_Code = F_RESSOURCEPROD.RP_Code\
                """;
        executeQuery(sqlCon, query);
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
                updateTableDest(sqlCon, "AR_Ref", "'AR_Ref','AR_SuiviStock'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));
                Condition.sendDataElement(sqlCon, path,unibase);
                RessourceProd.sendDataElement(sqlCon, path,unibase);
                ArticleRessource.sendDataElement(sqlCon, path,unibase);
                ArtCompta.sendDataElement(sqlCon, path,unibase);
                ArtClient.sendDataElement(sqlCon, path,unibase);
                ArtFourniss.sendDataElement(sqlCon, path,unibase);
                linkArticle(sqlCon);
                readOnFile(path, "deleteList" + filename, "F_ARTCLIENT_SUPPR", sqlCon);

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteTempTable(sqlCon, "F_ARTICLERESSOURCE_DEST");
                deleteTempTable(sqlCon, "F_CONDITION_DEST");
                deleteTempTable(sqlCon, "F_RESSOURCEPROD_DEST");

                deleteItem(sqlCon, path, filename);
                deleteArticle(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElementFilterAgency(Connection sqlCon, String path,String database,String time,String agency)
    {

        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref");//initTable(sqlCon);
        getData(sqlCon, selectSourceTableFilterAgencyArticle(tableName,database,agency), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
        Condition.getDataElement(sqlCon, path,database, time);
        ArticleRessource.getDataElement(sqlCon, path,database,time);
        RessourceProd.getDataElement(sqlCon, path,database, time);
        ArtCompta.getDataElement(sqlCon, path,database, time);
        ArtClient.getDataElement(sqlCon, path,database,time);
        ArtFourniss.getDataElement(sqlCon, path,database,time);

        DepotEmpl.getDataElement(sqlCon, path,database, time);
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref");//initTable(sqlCon);
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
        Condition.getDataElement(sqlCon, path,database, time);
        ArticleRessource.getDataElement(sqlCon, path,database,time);
        RessourceProd.getDataElement(sqlCon, path,database, time);
        ArtCompta.getDataElement(sqlCon, path,database, time);
        ArtClient.getDataElement(sqlCon, path,database,time);
        ArtFourniss.getDataElement(sqlCon, path,database,time);
    }

    public static void deleteArticle(Connection sqlCon,String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_ARTICLE
                         WHERE AR_Ref IN(SELECT AR_Ref FROM F_ARTICLE_SUPPR)

                         IF OBJECT_ID('F_ARTICLE_SUPPR') IS NOT NULL
                         DROP TABLE F_ARTICLE_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists()) {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
