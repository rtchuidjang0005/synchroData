import org.json.simple.JSONObject;

import java.sql.Connection;

public class DocLigne extends Table {

    public static String file = "DocLigne_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_DOCLIGNE";
    public static String configList = "listDocLigne";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_DOCLIGNE_DEST') IS NOT NULL
                BEGIN
                IF OBJECT_ID('tempdb..#DocLigne') IS NOT NULL
                    DROP TABLE #DocLigne

                 SELECT dest.[DO_Domaine],dest.[DO_Type],dest.[CT_Num],dest.[DO_Piece],dest.[DL_PieceBC],dest.[DL_PieceBL],dest.[DO_Date],dest.[DL_DateBC]
                        ,dest.[DL_DateBL],dest.[DL_Ligne]
                        ,[DO_Ref] = LEFT(CAST(dest.[DO_Ref] AS VARCHAR(150)),17),dest.[DL_TNomencl]
                        ,dest.[DL_TRemPied],dest.[DL_TRemExep],dest.[AR_Ref]
                        ,[DL_Design] = LEFT(dest.[DL_Design],69),dest.[DL_Qte],dest.[DL_QteBC],dest.[DL_QteBL]
                        ,dest.[DL_PoidsNet] ,dest.[DL_PoidsBrut]
                       ,dest.[DL_Remise01REM_Valeur] ,dest.[DL_Remise01REM_Type],dest.[DL_Remise02REM_Valeur],dest.[DL_Remise02REM_Type],dest.[DL_Remise03REM_Valeur]
                        ,dest.[DL_Remise03REM_Type]
                        ,dest.[DL_PrixUnitaire] ,dest.[DL_PUBC] ,dest.[DL_Taxe1] ,dest.[DL_TypeTaux1],dest.[DL_TypeTaxe1],dest.[DL_Taxe2] ,dest.[DL_TypeTaux2]
                        ,dest.[DL_TypeTaxe2],dest.[CO_No]/*,[AG_No1],[AG_No2]*/
                       ,dest.[DL_PrixRU] ,dest.[DL_CMUP] ,dest.[DL_MvtStock],dest.[DT_No],dest.[AF_RefFourniss],dest.[EU_Enumere],dest.[EU_Qte] ,dest.[DL_TTC]
                        ,DE_No = ISNULL(dsrc.[DE_No],dest.DE_No)
                        ,dest.[DL_NoRef],dest.[DL_TypePL],dest.[DL_PUDevise] ,dest.[DL_PUTTC]
                        ,[DL_No] = ISNULL((SELECT MAX(DL_No) FROM F_DOCLIGNE),0)+ ROW_NUMBER() OVER (ORDER BY dest.[cbMarqSource])
                        ,dest.[DO_DateLivr],dest.[CA_Num]
                        ,dest.[DL_Taxe3] ,dest.[DL_TypeTaux3],dest.[DL_TypeTaxe3],dest.[DL_Frais]
                       ,[DL_Valorise],[AR_RefCompose],[DL_NonLivre],[AC_RefClient],[DL_MontantHT]
                       ,[DL_MontantTTC] ,[DL_FactPoids],[DL_Escompte],[DL_PiecePL],[DL_DatePL],[DL_QtePL] ,[DL_NoColis],[DL_NoLink]
                       ,[RP_Code],[DL_QteRessource],[DL_DateAvancement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                       ,[DATEMODIF]
                        /*,[CONTROLE],[NBJ],[NOM_CLIENT],[PMIN],[PMAX],[CONTROLEDATE],[PGROS] */
                        ,[DL_QTE_SUPER_PRIX]
                       ,[DL_SUPER_PRIX],[USERGESCOM],[NOMCLIENT],[ORDONATEUR_REMISE],[GROUPEUSER],[MACHINEPC]
                       ,[Qte_LivreeBL],[Qte_RestantBL],[DL_COMM],dest.[cbMarqSource],dest.[DataBaseSource]
                       ,[cbMarqSourceSrc] = src.cbMarqSource
                       INTO #DocLigne
                 FROM F_DOCLIGNE_DEST dest
                 LEFT JOIN (SELECT cbMarqSource,dataBaseSource,DO_Piece,DO_Type,DO_Domaine FROM F_DOCLIGNE) src
                  ON ISNULL(dest.cbMarqSource,0) = ISNULL(src.cbMarqSource,0)
                  AND ISNULL(dest.dataBaseSource,'') = ISNULL(src.dataBaseSource,'')
                 LEFT JOIN (SELECT DE_NoSource,dataBaseSource,DE_No FROM F_DEPOT) dsrc
                  ON ISNULL(dsrc.DE_NoSource,0) = ISNULL(dest.DE_No,0)
                  AND ISNULL(dsrc.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')

                INSERT INTO F_DOCLIGNE (
                [DO_Domaine],[DO_Type],[CT_Num],[DO_Piece],[DL_PieceBC],[DL_PieceBL],[DO_Date],[DL_DateBC],[DL_DateBL],[DL_Ligne],[DO_Ref],[DL_TNomencl]
                      ,[DL_TRemPied],[DL_TRemExep],[AR_Ref],[DL_Design],[DL_Qte],[DL_QteBC],[DL_QteBL],[DL_PoidsNet] ,[DL_PoidsBrut]
                      ,[DL_Remise01REM_Valeur] ,[DL_Remise01REM_Type],[DL_Remise02REM_Valeur],[DL_Remise02REM_Type],[DL_Remise03REM_Valeur] ,[DL_Remise03REM_Type]
                      ,[DL_PrixUnitaire] ,[DL_PUBC] ,[DL_Taxe1] ,[DL_TypeTaux1],[DL_TypeTaxe1],[DL_Taxe2] ,[DL_TypeTaux2],[DL_TypeTaxe2],[CO_No]/*,[AG_No1],[AG_No2]*/
                      ,[DL_PrixRU] ,[DL_CMUP] ,[DL_MvtStock],[DT_No],[AF_RefFourniss],[EU_Enumere],[EU_Qte] ,[DL_TTC]
                      ,[DE_No],[DL_NoRef],[DL_TypePL],[DL_PUDevise] ,[DL_PUTTC] ,[DL_No],[DO_DateLivr],[CA_Num],[DL_Taxe3] ,[DL_TypeTaux3],[DL_TypeTaxe3],[DL_Frais]
                      ,[DL_Valorise],[AR_RefCompose],[DL_NonLivre],[AC_RefClient],[DL_MontantHT] ,[DL_MontantTTC] ,[DL_FactPoids],[DL_Escompte],[DL_PiecePL],[DL_DatePL],[DL_QtePL] ,[DL_NoColis],[DL_NoLink]
                      ,[RP_Code],[DL_QteRessource],[DL_DateAvancement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]      ,[DATEMODIF]\
                """ +
                //",[CONTROLE],[NBJ],[NOM_CLIENT],[PMIN],[PMAX],[CONTROLEDATE],[PGROS]" +
                """
                        ,[DL_QTE_SUPER_PRIX]      ,[DL_SUPER_PRIX],[USERGESCOM],[NOMCLIENT],[ORDONATEUR_REMISE],[GROUPEUSER],[Qte_LivreeBL],[Qte_RestantBL],[DL_COMM],[cbMarqSource],[DataBaseSource]      )

                        SELECT    [DO_Domaine],[DO_Type],[CT_Num],[DO_Piece],[DL_PieceBC],[DL_PieceBL],[DO_Date],[DL_DateBC]       ,[DL_DateBL],[DL_Ligne],[DO_Ref],[DL_TNomencl]
                               ,[DL_TRemPied],[DL_TRemExep],[AR_Ref],[DL_Design],[DL_Qte],[DL_QteBC],[DL_QteBL]       ,[DL_PoidsNet] ,[DL_PoidsBrut]
                                 ,[DL_Remise01REM_Valeur] ,[DL_Remise01REM_Type],[DL_Remise02REM_Valeur],[DL_Remise02REM_Type],[DL_Remise03REM_Valeur]        ,[DL_Remise03REM_Type]
                               ,[DL_PrixUnitaire] ,[DL_PUBC] ,[DL_Taxe1] ,[DL_TypeTaux1],[DL_TypeTaxe1],[DL_Taxe2] ,[DL_TypeTaux2]       ,[DL_TypeTaxe2],[CO_No]/*,[AG_No1],[AG_No2]*/
                                 ,[DL_PrixRU] ,[DL_CMUP] ,[DL_MvtStock],[DT_No],[AF_RefFourniss],[EU_Enumere],[EU_Qte] ,[DL_TTC]
                               ,DE_No,[DL_NoRef],[DL_TypePL],[DL_PUDevise] ,[DL_PUTTC]        ,DL_No,[DO_DateLivr],[CA_Num]       ,[DL_Taxe3] ,[DL_TypeTaux3],[DL_TypeTaxe3],[DL_Frais]
                              ,[DL_Valorise],[AR_RefCompose],[DL_NonLivre],[AC_RefClient],[DL_MontantHT] ,[DL_MontantTTC] ,[DL_FactPoids],[DL_Escompte],[DL_PiecePL],[DL_DatePL],[DL_QtePL] ,[DL_NoColis],[DL_NoLink]
                              ,[RP_Code],[DL_QteRessource],[DL_DateAvancement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]      ,[DATEMODIF]\
                        """ +
                // "       ,[CONTROLE],[NBJ],[NOM_CLIENT],[PMIN],[PMAX],[CONTROLEDATE],[PGROS]" +
                """
                               ,[DL_QTE_SUPER_PRIX]      ,[DL_SUPER_PRIX],[USERGESCOM],[NOMCLIENT],[ORDONATEUR_REMISE],[GROUPEUSER],[Qte_LivreeBL],[Qte_RestantBL],[DL_COMM],[cbMarqSource],[DataBaseSource]
                        FROM #DocLigne
                        WHERE cbMarqSourceSrc IS NULL
                        END

                        INSERT INTO config.DB_Errors(
                                  UserName,
                                  ErrorNumber,
                                  ErrorState,
                                  ErrorSeverity,
                                  ErrorLine,
                                  ErrorProcedure,
                                  ErrorMessage,
                                  TableLoad,
                                  Query,
                                  ErrorDateTime)
                        SELECT       SUSER_SNAME(),           NULL,           NULL,           NULL,           NULL,           NULL,           NULL,'Domaine : '+CAST(docL.[DO_Domaine] AS VARCHAR(150))+' Type : ' + CAST(docL.[DO_Type] AS VARCHAR(150)) + ' Piece : ' + CAST(docL.[DO_Piece] AS VARCHAR(150))           +' cbMarq : ' + CAST(docL.[cbMarqSource] AS VARCHAR(150)) + ' database : ' + CAST(docL.[DataBaseSource] AS VARCHAR(150))           + 'fileName : %s '           ,'Insert F_DOCLIGNE'
                                   ,GETDATE()
                        FROM #DocLigne docL
                        LEFT JOIN (SELECT DO_Piece,DO_Type,DO_Domaine FROM F_DOCENTETE) docE
                        ON docE.DO_Domaine = docL.DO_Domaine
                        AND docE.DO_Type = docL.DO_Type
                        AND docE.DO_Piece = docL.DO_Piece
                        WHERE cbMarqSourceSrc IS NULL
                        AND docE.DO_Piece IS NULL
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
                           'F_DOCLIGNE',
                           GETDATE());
                        END CATCH\
                        """.formatted(filename, filename);
    }
    public static void sendDataElement(Connection sqlCon, String path,String database,int unibase)
    {
        dbSource = database;
        loadFile(path,sqlCon);
        loadDeleteFile(path,sqlCon);
    }

    public static void loadFile(String path,Connection sqlCon){
        String [] children = getFile(path,file);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                disableTrigger(sqlCon,tableName);
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                if (isUpdateEnabled(tableName))
                    executeQuery(sqlCon, updateDocLigne(filename));
                sendData(sqlCon, path, filename, insert(filename));
                enableTrigger(sqlCon,tableName);
                //deleteTempTable(sqlCon, tableName+"_DEST");
            }
        }
    }

    public static String updateDocLigne(String filename){
        return """
                BEGIN TRY
                    SET DATEFORMAT ymd;

                    IF OBJECT_ID('F_DOCLIGNE_DEST') IS NOT NULL
                    BEGIN
                IF OBJECT_ID('tempdb..#DocLigne') IS NOT NULL
                    DROP TABLE #DocLigne

                 SELECT dest.[DO_Domaine],dest.[DO_Type],dest.[CT_Num],dest.[DO_Piece],dest.[DL_PieceBC],dest.[DL_PieceBL],dest.[DO_Date],dest.[DL_DateBC]
                        ,dest.[DL_DateBL],dest.[DL_Ligne]
                        ,[DO_Ref] = LEFT(CAST(dest.[DO_Ref] AS VARCHAR(150)),17),dest.[DL_TNomencl]
                        ,dest.[DL_TRemPied],dest.[DL_TRemExep],dest.[AR_Ref]
                        ,[DL_Design] = LEFT(dest.[DL_Design],69),dest.[DL_Qte],dest.[DL_QteBC],dest.[DL_QteBL]
                        ,dest.[DL_PoidsNet] ,dest.[DL_PoidsBrut]
                       ,dest.[DL_Remise01REM_Valeur] ,dest.[DL_Remise01REM_Type],dest.[DL_Remise02REM_Valeur],dest.[DL_Remise02REM_Type],dest.[DL_Remise03REM_Valeur]
                        ,dest.[DL_Remise03REM_Type]
                        ,dest.[DL_PrixUnitaire] ,dest.[DL_PUBC] ,dest.[DL_Taxe1] ,dest.[DL_TypeTaux1],dest.[DL_TypeTaxe1],dest.[DL_Taxe2] ,dest.[DL_TypeTaux2]
                        ,dest.[DL_TypeTaxe2],dest.[CO_No]/*,[AG_No1],[AG_No2]*/
                       ,dest.[DL_PrixRU] ,dest.[DL_CMUP] ,dest.[DL_MvtStock],dest.[DT_No],dest.[AF_RefFourniss],dest.[EU_Enumere],dest.[EU_Qte] ,dest.[DL_TTC]
                        ,DE_No = ISNULL(dsrc.[DE_No],dest.DE_No)
                        ,dest.[DL_NoRef],dest.[DL_TypePL],dest.[DL_PUDevise] ,dest.[DL_PUTTC]
                        ,[DL_No] = ISNULL((SELECT MAX(DL_No) FROM F_DOCLIGNE),0)+ ROW_NUMBER() OVER (ORDER BY dest.[cbMarqSource])
                        ,dest.[DO_DateLivr],dest.[CA_Num]
                        ,dest.[DL_Taxe3] ,dest.[DL_TypeTaux3],dest.[DL_TypeTaxe3],dest.[DL_Frais]
                       ,[DL_Valorise],[AR_RefCompose],[DL_NonLivre],[AC_RefClient],[DL_MontantHT]
                       ,[DL_MontantTTC] ,[DL_FactPoids],[DL_Escompte],[DL_PiecePL],[DL_DatePL],[DL_QtePL] ,[DL_NoColis],[DL_NoLink]
                       ,[RP_Code],[DL_QteRessource],[DL_DateAvancement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                       ,[DATEMODIF]
                        /*,[CONTROLE],[NBJ],[NOM_CLIENT],[PMIN],[PMAX],[CONTROLEDATE],[PGROS] */
                        ,[DL_QTE_SUPER_PRIX]
                       ,[DL_SUPER_PRIX],[USERGESCOM],[NOMCLIENT],[ORDONATEUR_REMISE],[GROUPEUSER],[MACHINEPC]
                       ,[Qte_LivreeBL],[Qte_RestantBL],[DL_COMM],dest.[cbMarqSource],dest.[DataBaseSource]
                       ,[cbMarqSourceSrc] = src.cbMarqSource
                       INTO #DocLigne
                 FROM F_DOCLIGNE_DEST dest
                 LEFT JOIN (SELECT cbMarqSource,dataBaseSource,DO_Piece,DO_Type,DO_Domaine FROM F_DOCLIGNE) src
                  ON ISNULL(dest.cbMarqSource,0) = ISNULL(src.cbMarqSource,0)
                  AND ISNULL(dest.dataBaseSource,'') = ISNULL(src.dataBaseSource,'')
                 LEFT JOIN (SELECT DE_NoSource,dataBaseSource,DE_No FROM F_DEPOT) dsrc
                  ON ISNULL(dsrc.DE_NoSource,0) = ISNULL(dest.DE_No,0)
                  AND ISNULL(dsrc.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')

                        UPDATE doc
                        SET DO_Domaine = docL.DO_Domaine
                            ,DO_Type = docL.DO_Type
                            ,CT_Num = docL.CT_Num
                            ,DO_Piece = docL.DO_Piece
                            ,DL_PieceBC = docL.DL_PieceBC
                            ,DL_PieceBL = docL.DL_PieceBL
                            ,DO_Date = docL.DO_Date
                            ,DL_DateBC = docL.DL_DateBC
                            ,DL_DateBL = docL.DL_DateBL
                            ,DL_Ligne = docL.DL_Ligne
                            ,DO_Ref = docL.DO_Ref
                            ,DL_TNomencl = docL.DL_TNomencl
                            ,DL_TRemPied = docL.DL_TRemPied
                            ,DL_TRemExep = docL.DL_TRemExep
                            ,DL_Design = docL.DL_Design
                            ,DL_Qte = docL.DL_Qte
                            ,DL_QteBC = docL.DL_QteBC
                            ,DL_QteBL = docL.DL_QteBL
                            ,DL_PoidsNet = docL.DL_PoidsNet
                            ,DL_PoidsBrut = docL.DL_PoidsBrut
                            ,DL_Remise01REM_Valeur = docL.DL_Remise01REM_Valeur
                            ,DL_Remise01REM_Type = docL.DL_Remise01REM_Type
                            ,DL_Remise02REM_Valeur = docL.DL_Remise02REM_Valeur
                            ,DL_Remise02REM_Type = docL.DL_Remise02REM_Type
                            ,DL_Remise03REM_Valeur = docL.DL_Remise03REM_Valeur
                            ,DL_Remise03REM_Type = docL.DL_Remise03REM_Type
                            ,DL_PrixUnitaire = docL.DL_PrixUnitaire
                            ,DL_PUBC = docL.DL_PUBC
                            ,DL_Taxe1 = docL.DL_Taxe1
                            ,DL_TypeTaux1 = docL.DL_TypeTaux1
                            ,DL_TypeTaxe1 = docL.DL_TypeTaxe1
                            ,DL_Taxe2 = docL.DL_Taxe2
                            ,DL_TypeTaux2 = docL.DL_TypeTaux2
                            ,DL_TypeTaxe2 = docL.DL_TypeTaxe2
                            ,CO_No = docL.CO_No
                            ,DL_PrixRU = docL.DL_PrixRU
                            ,DL_CMUP = docL.DL_CMUP
                            ,DL_MvtStock = docL.DL_MvtStock
                            ,DT_No = docL.DT_No
                            ,AF_RefFourniss = docL.AF_RefFourniss
                            ,EU_Enumere = docL.EU_Enumere
                            ,EU_Qte = docL.EU_Qte
                            ,DL_TTC = docL.DL_TTC
                            ,DE_No = docL.DE_No
                            ,DL_NoRef = docL.DL_NoRef
                            ,DL_TypePL = docL.DL_TypePL
                            ,DL_PUDevise = docL.DL_PUDevise
                            ,DL_PUTTC = docL.DL_PUTTC
                            ,DO_DateLivr = docL.DO_DateLivr
                            ,CA_Num = docL.CA_Num
                            ,DL_Taxe3 = docL.DL_Taxe3
                            ,DL_TypeTaux3 = docL.DL_TypeTaux3
                            ,DL_TypeTaxe3 = docL.DL_TypeTaxe3
                            ,DL_Frais = docL.DL_Frais
                            ,DL_Valorise = docL.DL_Valorise
                            ,AR_RefCompose = docL.AR_RefCompose
                            ,DL_NonLivre = docL.DL_NonLivre
                            ,AC_RefClient = docL.AC_RefClient
                            ,DL_MontantHT = docL.DL_MontantHT
                            ,DL_MontantTTC = docL.DL_MontantTTC
                            ,DL_FactPoids = docL.DL_FactPoids
                            ,DL_Escompte = docL.DL_Escompte
                            ,DL_PiecePL = docL.DL_PiecePL
                            ,DL_DatePL = docL.DL_DatePL
                            ,DL_QtePL = docL.DL_QtePL
                            ,DL_NoColis = docL.DL_NoColis
                            ,DL_NoLink = docL.DL_NoLink
                            ,RP_Code = docL.RP_Code
                            ,DL_QteRessource = docL.DL_QteRessource
                            ,DL_DateAvancement = docL.DL_DateAvancement
                            ,DATEMODIF = docL.DATEMODIF
                """ +
                /*"\t\t\t,CONTROLE = docL.CONTROLE\n" +
                "\t\t\t,NBJ = docL.NBJ\n" +
                "\t\t\t,NOM_CLIENT = docL.NOM_CLIENT\n" +
                "\t\t\t,PMIN = docL.PMIN\n" +
                "\t\t\t,PMAX = docL.PMAX\n" +
                "\t\t\t,CONTROLEDATE = docL.CONTROLEDATE\n" +
                "\t\t\t,PGROS = docL.PGROS\n" +*/
                """
                                    ,DL_QTE_SUPER_PRIX = docL.DL_QTE_SUPER_PRIX
                                    ,DL_SUPER_PRIX = docL.DL_SUPER_PRIX
                                    ,NOMCLIENT = docL.NOMCLIENT
                                    ,ORDONATEUR_REMISE = docL.ORDONATEUR_REMISE
                                    ,GROUPEUSER = docL.GROUPEUSER
                        """ +
//                "\t\t\t,Qte_LivreeBL = docL.Qte_LivreeBL\n" +
//                "\t\t\t,Qte_RestantBL = docL.Qte_RestantBL\n" +
                """
                                    ,DL_COMM = docL.DL_COMM
                                    ,DataBaseSource = docL.DataBaseSource
                                    ,USERGESCOM = docL.USERGESCOM
                                    ,MACHINEPC = docL.MACHINEPC
                        FROM F_DOCLIGNE doc
                                INNER JOIN #DocLigne docL ON ISNULL(doc.cbMarqSource,0) = ISNULL(docL.cbMarqSource,0)
                        AND ISNULL(doc.[DataBaseSource],'') = ISNULL(docL.[DataBaseSource],'')

                                INSERT INTO config.DB_Errors (
                                    UserName
                                    ,ErrorNumber
                                    ,ErrorState
                                    ,ErrorSeverity
                                    ,ErrorLine
                                    ,ErrorProcedure
                                    ,ErrorMessage
                                    ,TableLoad
                                    ,Query
                                    ,ErrorDateTime
                                    )
                                SELECT SUSER_SNAME()
                                    ,NULL
                                    ,NULL
                                    ,NULL
                                    ,NULL
                                    ,NULL
                                    ,NULL
                                    ,'Domaine : ' + CAST(dest.[DO_Domaine] AS VARCHAR(150)) + ' Type : ' + CAST(dest.[DO_Type] AS VARCHAR(150)) + ' Piece : ' + CAST(dest.[DO_Piece] AS VARCHAR(150)) + ' cbMarq : ' + CAST(dest.[cbMarqSource] AS VARCHAR(150)) + ' database : ' + CAST(dest.[DataBaseSource] AS VARCHAR(150)) + 'fileName : %s '
                                    ,'Update F_DOCLIGNE'
                                    ,GETDATE()
                                FROM F_DOCLIGNE_DEST dest
                                INNER JOIN F_DOCLIGNE docL ON ISNULL(docL.cbMarqSource,0) = ISNULL(dest.cbMarqSource,0)
                                    AND ISNULL(docL.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                        LEFT JOIN (SELECT DE_NoSource,dataBaseSource,DE_No FROM F_DEPOT) dsrc
                        ON    ISNULL(dsrc.DE_NoSource,0) = ISNULL(dest.DE_No,0)
                        AND    ISNULL(dsrc.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')
                        LEFT JOIN F_DOCENTETE docE ON docE.DO_Type = dest.DO_Type
                        AND docE.DO_Piece = dest.DO_Piece
                        AND docE.DO_Domaine = dest.DO_Domaine
                        WHERE docE.DO_Type IS NULL
                            END
                        END TRY

                        BEGIN CATCH
                            INSERT INTO config.DB_Errors
                            VALUES (
                                SUSER_SNAME()
                                ,ERROR_NUMBER()
                                ,ERROR_STATE()
                                ,ERROR_SEVERITY()
                                ,ERROR_LINE()
                                ,ERROR_PROCEDURE()
                                ,ERROR_MESSAGE()
                                ,'Insert ' + ' %s'
                                ,'F_DOCLIGNE'
                                ,GETDATE()
                                );
                        END CATCH
                        """.formatted(filename, filename);
    }
    public static void loadDeleteFile(String path,Connection sqlCon) {
        String [] children = getFile(path,"deleteList"+file);
        disableTrigger(sqlCon,tableName);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                String deleteDocLigne = deleteDocLigne();
                loadDeleteInfo(path,tableName,filename,sqlCon,deleteDocLigne);
            }
        }
        enableTrigger(sqlCon,tableName);
    }

    public static void getDataElement(Connection sqlCon, String path, String database, String time, JSONObject type)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"DO_Domaine,DO_Type,DO_Piece,DatabaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,type), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void getDataElementFilterAgency(Connection sqlCon, String path,String database,String time,String agency)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"DO_Domaine,DO_Type,DO_Piece,DatabaseSource");
        getData(sqlCon, selectSourceTableFilterAgencyEnteteLink(tableName,database,agency), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static String deleteDocLigne()
    {
        return
                """
                         DELETE FROM F_DOCLIGNE
                         WHERE EXISTS (SELECT 1                FROM F_DOCLIGNE_SUPPR                WHERE ISNULL(F_DOCLIGNE.cbMarqSource,0) = ISNULL(F_DOCLIGNE_SUPPR.cbMarq,0)                AND ISNULL(F_DOCLIGNE.DataBaseSource,'') = ISNULL(F_DOCLIGNE_SUPPR.DataBaseSource,''))
                        """
                ;

    }
}
