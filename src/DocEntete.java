import org.json.simple.JSONObject;

import java.sql.Connection;

public class DocEntete extends Table {
    public static String file = "docEntete_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_DOCENTETE";
    public static String configList = "listDocEntete";

    public static String insert(String filename)
    {
        return """
                 BEGIN TRY
                                  SET DATEFORMAT ymd;
                                  IF OBJECT_ID('F_DOCENTETE_DEST') IS NOT NULL
                                 BEGIN
                                 IF OBJECT_ID('tempdb..#DocEntete') IS NOT NULL
                                     DROP TABLE #DocEntete
                                 -- Pièces non reprises : le numéro existe déjà pour un autre document (autre base ou document local)
                                 INSERT INTO config.DB_Errors
                                 SELECT SUSER_SNAME(), NULL, NULL, NULL, NULL, NULL
                                      , 'Pièce déjà existante, document non repris'
                                      , 'Domaine : ' + CAST(dest.DO_Domaine AS NVARCHAR(10)) + ' Type : ' + CAST(dest.DO_Type AS NVARCHAR(10))
                                        + ' Piece : ' + dest.DO_Piece + ' cbMarq : ' + CAST(dest.cbMarqSource AS NVARCHAR(20))
                                        + ' database : ' + ISNULL(dest.DataBaseSource,'') + ' existant : ' + ISNULL(src.DataBaseSource,'(local)')
                                        + ' fileName : %s'
                                      , 'F_DOCENTETE', GETDATE()
                                 FROM F_DOCENTETE_DEST dest
                                 INNER JOIN F_DOCENTETE src
                                 ON dest.DO_Piece = src.DO_Piece
                                 AND dest.DO_Domaine = src.DO_Domaine
                                 AND dest.DO_Type = src.DO_Type
                                 WHERE NOT (ISNULL(src.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                                            AND ISNULL(src.cbMarqSource,0) = ISNULL(dest.cbMarqSource,0))
                                 SELECT dest.[DO_Domaine], dest.[DO_Type], dest.[DO_Date],[DO_Ref] = LEFT(dest.[DO_Ref],17)
                                 , dest.[DO_Tiers], dest.[CO_No], dest.[DO_Period], dest.[DO_Devise]
                                 , dest.[DO_Cours]
                                 , DE_No = ISNULL(dep.DE_No,dest.[DE_No])
                                 , LI_No = ISNULL(liv.[LI_No],dest.[LI_No])
                                 , dest.[CT_NumPayeur], dest.[DO_Expedit], dest.[DO_NbFacture], dest.[DO_BLFact]
                                 , dest.[DO_TxEscompte], dest.[DO_Reliquat], dest.[DO_Imprim], dest.[CA_Num]
                                 , dest.[DO_Coord01], dest.[DO_Coord02], dest.[DO_Coord03], dest.[DO_Coord04]
                                 , dest.[DO_Souche], dest.[DO_DateLivr], dest.[DO_Condition], dest.[DO_Tarif]
                                 , dest.[DO_Colisage], dest.[DO_TypeColis], dest.[DO_Transaction], dest.[DO_Langue]
                                 , dest.[DO_Ecart], dest.[DO_Regime], dest.[N_CatCompta], dest.[DO_Ventile]
                                 , dest.[AB_No], dest.[DO_DebutAbo], dest.[DO_FinAbo], dest.[DO_DebutPeriod]
                                 , dest.[DO_FinPeriod], dest.[CG_Num], dest.[DO_Statut], dest.[DO_Heure]
                                 , CA_No = ISNULL(cai.CA_No,dest.[CA_No]), dest.[CO_NoCaissier]
                                 , dest.[DO_Transfere], dest.[DO_Cloture], dest.[DO_NoWeb], dest.[DO_Attente]
                                 , dest.[DO_Provenance], dest.[CA_NumIFRS], dest.[MR_No], dest.[DO_TypeFrais]
                                 , dest.[DO_ValFrais], dest.[DO_TypeLigneFrais], dest.[DO_TypeFranco], dest.[DO_ValFranco]
                                 , dest.[DO_TypeLigneFranco], dest.[DO_Taxe1], dest.[DO_TypeTaux1], dest.[DO_TypeTaxe1]
                                 , dest.[DO_Taxe2], dest.[DO_TypeTaux2], dest.[DO_TypeTaxe2], dest.[DO_Taxe3]
                                 , dest.[DO_TypeTaux3], dest.[DO_TypeTaxe3], dest.[DO_MajCpta], dest.[DO_Motif]
                                 , dest.[CT_NumCentrale], dest.[DO_Contact], dest.[DO_FactureElec], dest.[DO_TypeTransac]
                                 , dest.[cbProt], dest.[cbCreateur], dest.[cbModification], dest.[cbReplication]
                                 , dest.[cbFlag],dest.[DO_Piece],dest.[dataBaseSource],dest.[cbMarqSource],DO_DomaineSrc = src.DO_Domaine
                                 INTO #DocEntete
                                 FROM F_DOCENTETE_DEST dest
                                 LEFT JOIN F_DOCENTETE src
                                 ON dest.DO_Piece = src.DO_Piece
                                 AND dest.DO_Domaine = src.DO_Domaine
                                 AND dest.DO_Type = src.DO_Type
                                 LEFT JOIN F_DEPOT dep ON ISNULL(dep.DE_NoSource,0) = ISNULL(dest.DE_No,0) AND ISNULL(dep.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')
                                 LEFT JOIN F_CAISSE cai ON ISNULL(cai.CA_NoSource,0) = ISNULL(dest.CA_No,0) AND ISNULL(cai.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')
                                 -- une adresse de livraison appartient à un tiers : évite de dupliquer le document si LI_NoSource est en double
                                 LEFT JOIN F_LIVRAISON liv ON ISNULL(liv.LI_NoSource,0) = ISNULL(dest.LI_No,0) AND ISNULL(liv.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')
                                 AND liv.CT_Num = dest.DO_Tiers
                                 WHERE src.DO_Domaine IS NULL
                                 {{FK_CHECK}}
                                 IF {{UPDATE_ENABLED}} = 1
                                 UPDATE dwh
                                    SET [DO_Domaine] = tmp.DO_Domaine
                                       ,[DO_Type] = tmp.DO_Type
                                       ,[DO_Piece] = tmp.DO_Piece
                                       ,[DO_Date] = tmp.DO_Date
                                       ,[DO_Ref] = tmp.DO_Ref
                                       ,[DO_Tiers] = tmp.DO_Tiers
                                       ,[CO_No] = tmp.CO_No
                                       ,[DO_Period] = tmp.DO_Period
                                       ,[DO_Devise] = tmp.DO_Devise
                                       ,[DO_Cours] = tmp.DO_Cours
                                       ,[DE_No] = tmp.DE_No
                                       ,[LI_No] = tmp.LI_No
                                       ,[CT_NumPayeur] = tmp.CT_NumPayeur
                                       ,[DO_Expedit] = tmp.DO_Expedit
                                       ,[DO_NbFacture] = tmp.DO_NbFacture
                                       ,[DO_BLFact] = tmp.DO_BLFact
                                       ,[DO_TxEscompte] = tmp.DO_TxEscompte
                                       ,[DO_Reliquat] = tmp.DO_Reliquat
                                       ,[DO_Imprim] = tmp.DO_Imprim
                                       ,[CA_Num] = tmp.CA_Num
                                       ,[DO_Coord01] = tmp.DO_Coord01
                                       ,[DO_Coord02] = tmp.DO_Coord02
                                       ,[DO_Coord03] = tmp.DO_Coord03
                                       ,[DO_Coord04] = tmp.DO_Coord04
                                       ,[DO_Souche] = tmp.DO_Souche
                                       ,[DO_DateLivr] = tmp.DO_DateLivr
                                       ,[DO_Condition] = tmp.DO_Condition
                                       ,[DO_Tarif] = tmp.DO_Tarif
                                       ,[DO_Colisage] = tmp.DO_Colisage
                                       ,[DO_TypeColis] = tmp.DO_TypeColis
                                       ,[DO_Transaction] = tmp.DO_Transaction
                                       ,[DO_Langue] = tmp.DO_Langue
                                       ,[DO_Ecart] = tmp.DO_Ecart
                                       ,[DO_Regime] = tmp.DO_Regime
                                       ,[N_CatCompta] = tmp.N_CatCompta
                                       ,[DO_Ventile] = tmp.DO_Ventile
                                       ,[AB_No] = tmp.AB_No
                                       ,[DO_DebutAbo] = tmp.DO_DebutAbo
                                       ,[DO_FinAbo] = tmp.DO_FinAbo
                                       ,[DO_DebutPeriod] = tmp.DO_DebutPeriod
                                       ,[DO_FinPeriod] = tmp.DO_FinPeriod
                                       ,[CG_Num] = tmp.CG_Num
                                       ,[DO_Statut] = tmp.DO_Statut
                                       ,[DO_Heure] = tmp.DO_Heure
                                       ,[CA_No] = tmp.CA_No
                                       ,[CO_NoCaissier] = tmp.CO_NoCaissier
                                       ,[DO_Transfere] = tmp.DO_Transfere
                                       ,[DO_Cloture] = tmp.DO_Cloture
                                       ,[DO_NoWeb] = tmp.DO_NoWeb
                                       ,[DO_Attente] = tmp.DO_Attente
                                       ,[DO_Provenance] = tmp.DO_Provenance
                                       ,[CA_NumIFRS] = tmp.CA_NumIFRS
                                       ,[MR_No] = tmp.MR_No
                                       ,[DO_TypeFrais] = tmp.DO_TypeFrais
                                       ,[DO_ValFrais] = tmp.DO_ValFrais
                                       ,[DO_TypeLigneFrais] = tmp.DO_TypeLigneFrais
                                       ,[DO_TypeFranco] = tmp.DO_TypeFranco
                                       ,[DO_ValFranco] = tmp.DO_ValFranco
                                       ,[DO_TypeLigneFranco] = tmp.DO_TypeLigneFranco
                                       ,[DO_Taxe1] = tmp.DO_Taxe1
                                       ,[DO_TypeTaux1] = tmp.DO_TypeTaux1
                                       ,[DO_TypeTaxe1] = tmp.DO_TypeTaxe1
                                       ,[DO_Taxe2] = tmp.DO_Taxe2
                                       ,[DO_TypeTaux2] = tmp.DO_TypeTaux2
                                       ,[DO_TypeTaxe2] = tmp.DO_TypeTaxe2
                                       ,[DO_Taxe3] = tmp.DO_Taxe3
                                       ,[DO_TypeTaux3] = tmp.DO_TypeTaux3
                                       ,[DO_TypeTaxe3] = tmp.DO_TypeTaxe3
                                       ,[DO_MajCpta] = tmp.DO_MajCpta
                                       ,[DO_Motif] = tmp.DO_Motif
                                       ,[CT_NumCentrale] = tmp.CT_NumCentrale
                                       ,[DO_Contact] = tmp.DO_Contact
                                       ,[DO_FactureElec] = tmp.DO_FactureElec
                                       ,[DO_TypeTransac] = tmp.DO_TypeTransac
                                       ,[cbProt] = tmp.cbProt
                                       ,[cbCreateur] = tmp.cbCreateur
                                       ,[cbModification] = tmp.cbModification
                                       ,[cbReplication] = tmp.cbReplication
                                       ,[cbFlag] = tmp.cbFlag

                                 FROM [dbo].[F_DOCENTETE] dwh
                                 INNER JOIN #DocEntete tmp ON ISNULL(dwh.[DataBaseSource],'') = ISNULL(tmp.[DataBaseSource],'')
                                 AND ISNULL(dwh.cbMarqSource,0) = ISNULL(tmp.cbMarqSource,0)

                                  INSERT INTO F_DOCENTETE (
                                 [DO_Domaine], [DO_Type], [DO_Date], [DO_Ref]
                                  , [DO_Tiers], [CO_No], [DO_Period], [DO_Devise]
                                  , [DO_Cours], [DE_No], [LI_No]
                                  , [CT_NumPayeur], [DO_Expedit], [DO_NbFacture], [DO_BLFact]
                                  , [DO_TxEscompte], [DO_Reliquat], [DO_Imprim], [CA_Num]
                                  , [DO_Coord01], [DO_Coord02], [DO_Coord03], [DO_Coord04]
                                  , [DO_Souche], [DO_DateLivr], [DO_Condition], [DO_Tarif]
                                  , [DO_Colisage], [DO_TypeColis], [DO_Transaction], [DO_Langue]
                                  , [DO_Ecart], [DO_Regime], [N_CatCompta], [DO_Ventile]
                                  , [AB_No], [DO_DebutAbo], [DO_FinAbo], [DO_DebutPeriod]
                                  , [DO_FinPeriod], [CG_Num], [DO_Statut], [DO_Heure]
                                  , [CA_No], [CO_NoCaissier]
                                  , [DO_Transfere], [DO_Cloture], [DO_NoWeb], [DO_Attente]
                                  , [DO_Provenance], [CA_NumIFRS], [MR_No], [DO_TypeFrais]
                                  , [DO_ValFrais], [DO_TypeLigneFrais], [DO_TypeFranco], [DO_ValFranco]
                                   , [DO_TypeLigneFranco], [DO_Taxe1], [DO_TypeTaux1], [DO_TypeTaxe1]
                                   , [DO_Taxe2], [DO_TypeTaux2], [DO_TypeTaxe2], [DO_Taxe3]
                                   , [DO_TypeTaux3], [DO_TypeTaxe3], [DO_MajCpta], [DO_Motif]
                                    , [CT_NumCentrale], [DO_Contact], [DO_FactureElec], [DO_TypeTransac]
                                    , [cbProt], [cbCreateur], [cbModification], [cbReplication]
                                    , [cbFlag],[DO_Piece],[DataBaseSource],[cbMarqSource])

                                 SELECT docE.[DO_Domaine], docE.[DO_Type], docE.[DO_Date], docE.[DO_Ref]
                                  , docE.[DO_Tiers], docE.[CO_No], docE.[DO_Period], docE.[DO_Devise]
                                  , docE.[DO_Cours], docE.DE_No, docE.[LI_No]
                                  , [CT_NumPayeur], [DO_Expedit], [DO_NbFacture], [DO_BLFact]
                                  , [DO_TxEscompte], [DO_Reliquat], [DO_Imprim], [CA_Num]
                                  , [DO_Coord01], [DO_Coord02], [DO_Coord03], [DO_Coord04]
                                  , [DO_Souche], [DO_DateLivr], [DO_Condition], [DO_Tarif]
                                  , [DO_Colisage], [DO_TypeColis], [DO_Transaction], [DO_Langue]
                                  , [DO_Ecart], [DO_Regime], [N_CatCompta], [DO_Ventile]
                                  , [AB_No], [DO_DebutAbo], [DO_FinAbo], [DO_DebutPeriod]
                                  , [DO_FinPeriod], [CG_Num], [DO_Statut], [DO_Heure]
                                  , docE.CA_No, [CO_NoCaissier]
                                  , [DO_Transfere], [DO_Cloture], [DO_NoWeb], [DO_Attente]
                                  , [DO_Provenance], [CA_NumIFRS], [MR_No], [DO_TypeFrais]
                                  , [DO_ValFrais], [DO_TypeLigneFrais], [DO_TypeFranco], [DO_ValFranco]
                                   , [DO_TypeLigneFranco], [DO_Taxe1], [DO_TypeTaux1], [DO_TypeTaxe1]
                                   , [DO_Taxe2], [DO_TypeTaux2], [DO_TypeTaxe2], [DO_Taxe3]
                                   , [DO_TypeTaux3], [DO_TypeTaxe3], [DO_MajCpta], [DO_Motif]
                                    , [CT_NumCentrale], [DO_Contact], [DO_FactureElec], [DO_TypeTransac]
                                    , [cbProt], [cbCreateur], [cbModification], [cbReplication]
                                    , [cbFlag],[DO_Piece],[dataBaseSource],[cbMarqSource]
                                 FROM #DocEntete docE
                                 WHERE DO_DomaineSrc IS NULL;
                                 END
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
                   'F_DOCENTETE',
                   GETDATE());
                END CATCH\
                """.formatted(filename, filename)
                .replace("{{FK_CHECK}}", foreignKeyCheck(tableName, "#DocEntete",
                        "N'Domaine : ' + CAST(t.DO_Domaine AS NVARCHAR(10)) + N' Type : ' + CAST(t.DO_Type AS NVARCHAR(10))"
                                + " + N' Piece : ' + t.DO_Piece + N' cbMarq : ' + CAST(t.cbMarqSource AS NVARCHAR(20))"
                                + " + N' database : ' + ISNULL(t.DataBaseSource, N'')",
                        filename));
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
            for (String filename : children) {
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                executeQuery(sqlCon, renameExistingPieceEntete(filename));
                disableTrigger(sqlCon,tableName);
//                executeQuery(sqlCon, updateTableDest("", "'DO_Domaine','DO_Type','DO_Piece','DataBaseSource','cbMarqSource'", tableName, tableName + "_DEST",filename));
                sendData(sqlCon, path, filename, applyUpdateFlag(insert(filename), tableName));
                //deleteTempTable(sqlCon, tableName+"_DEST");
                enableTrigger(sqlCon,tableName);
            }
        }
    }

    public static void loadDeleteFile(String path,Connection sqlCon) {
        String [] children = getFile(path,"deleteList"+file);
        disableTrigger(sqlCon,tableName);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                String deleteDocEntete = deleteDocEntete(filename);
                loadDeleteInfo(path,tableName,filename,sqlCon,deleteDocEntete);
            }
        }
        enableTrigger(sqlCon,tableName);
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time,JSONObject type)
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
        getData(sqlCon, selectSourceTableFilterAgency(tableName,database,agency,"DE_No"), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static String deleteDocEntete(String filename)
    {
        return
                "BEGIN TRY " +
                /*" DELETE FROM F_DOCLIGNE  " +
                "   WHERE EXISTS (SELECT 1 "+
                "       FROM F_DOCENTETE_SUPPR suppr\n" +
                "       WHERE F_DOCLIGNE.DataBaseSource = suppr.DataBaseSource \n" +
                "       AND F_DOCLIGNE.DO_Domaine = suppr.DO_Domaine \n" +
                "       AND F_DOCLIGNE.DO_Type = suppr.DO_Type \n" +
                "       AND F_DOCLIGNE.DO_Piece = suppr.DO_Piece ); \n" +
                " DELETE FROM F_DOCREGL  " +
                "   WHERE EXISTS (SELECT 1 "+
                "       FROM F_DOCENTETE_SUPPR suppr\n" +
                "       WHERE F_DOCREGL.DataBaseSource = suppr.DataBaseSource \n" +
                "       AND F_DOCREGL.DO_Domaine = suppr.DO_Domaine \n" +
                "       AND F_DOCREGL.DO_Type = suppr.DO_Type \n" +
                "       AND F_DOCREGL.DO_Piece = suppr.DO_Piece ); \n" +
                " DELETE FROM F_REGLECH  " +
                "   WHERE EXISTS (SELECT 1 "+
                "       FROM F_DOCENTETE_SUPPR suppr\n" +
                "       WHERE F_REGLECH.DataBaseSource = suppr.DataBaseSource \n" +
                "       AND F_REGLECH.DO_Domaine = suppr.DO_Domaine \n" +
                "       AND F_REGLECH.DO_Type = suppr.DO_Type \n" +
                "       AND F_REGLECH.DO_Piece = suppr.DO_Piece ); \n" +*/
                """

                        IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TG_CBDEL_F_DOCENTETE' AND parent_id = OBJECT_ID('dbo.F_DOCENTETE'))
                        BEGIN
                            DISABLE TRIGGER dbo.TG_CBDEL_F_DOCENTETE ON [dbo].[F_DOCENTETE];
                        END
                        IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TG_DEL_F_DOCENTETE' AND parent_id = OBJECT_ID('dbo.F_DOCENTETE'))
                        BEGIN
                            DISABLE TRIGGER dbo.TG_DEL_F_DOCENTETE ON [dbo].[F_DOCENTETE];
                        END
""" + renameExistingPieceLink("F_DOCENTETE_SUPPR") + """
                         DELETE FROM F_DOCENTETE
                         WHERE EXISTS (SELECT 1
                               FROM F_DOCENTETE_SUPPR
                               WHERE ISNULL(F_DOCENTETE.DataBaseSource,'') = ISNULL(F_DOCENTETE_SUPPR.DataBaseSource,'')
                               AND F_DOCENTETE.DO_Domaine = F_DOCENTETE_SUPPR.DO_Domaine
                               AND F_DOCENTETE.DO_Type = F_DOCENTETE_SUPPR.DO_Type
                               AND F_DOCENTETE.DO_Piece = F_DOCENTETE_SUPPR.DO_Piece ) ;
                        """ +
                /*" AND NOT EXISTS (SELECT 1 \n" +
                "       FROM F_DOCLIGNE docL \n" +
                "       WHERE F_DOCENTETE.DataBaseSource = docL.DataBaseSource \n" +
                "       AND F_DOCENTETE.DO_Domaine = docL.DO_Domaine \n" +
                "       AND F_DOCENTETE.DO_Type = docL.DO_Type \n" +
                "       AND F_DOCENTETE.DO_Piece = docL.DO_Piece ) \n" +
                " AND NOT EXISTS (SELECT 1 \n" +
                "       FROM F_DOCREGL docL \n" +
                "       WHERE F_DOCENTETE.DataBaseSource = docL.DataBaseSource \n" +
                "       AND F_DOCENTETE.DO_Domaine = docL.DO_Domaine \n" +
                "       AND F_DOCENTETE.DO_Type = docL.DO_Type \n" +
                "       AND F_DOCENTETE.DO_Piece = docL.DO_Piece ) \n" +
                " AND NOT EXISTS (SELECT 1 \n" +
                "       FROM F_REGLECH docL \n" +
                "       WHERE F_DOCENTETE.DataBaseSource = docL.DataBaseSource \n" +
                "       AND F_DOCENTETE.DO_Domaine = docL.DO_Domaine \n" +
                "       AND F_DOCENTETE.DO_Type = docL.DO_Type \n" +
                "       AND F_DOCENTETE.DO_Piece = docL.DO_Piece ) \n" +*/
                """
                        IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TG_CBDEL_F_DOCENTETE' AND parent_id = OBJECT_ID('dbo.F_DOCENTETE'))
                        BEGIN
                            ENABLE TRIGGER dbo.TG_CBDEL_F_DOCENTETE ON [dbo].[F_DOCENTETE];
                        END
                        IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TG_DEL_F_DOCENTETE' AND parent_id = OBJECT_ID('dbo.F_DOCENTETE'))
                        BEGIN
                            ENABLE TRIGGER dbo.TG_DEL_F_DOCENTETE ON [dbo].[F_DOCENTETE];
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
                           SELECT       SUSER_SNAME(),           NULL,           NULL,           NULL,           NULL,           NULL,           NULL,'Domaine : '+ CAST(docE.[DO_Domaine] AS NVARCHAR(50)) +' Type : ' + CAST(docE.[DO_Type] AS NVARCHAR(50)) + ' Piece : ' + docE.[DO_Piece]           +' cbMarq : ' + CAST(docE.[cbMarqSource]  AS NVARCHAR(50)) + ' database : ' + docE.[DataBaseSource]           + ' fileName : %s '           ,'F_DOCLIGNE'
                                   ,GETDATE()
                         FROM F_DOCENTETE docE
                         WHERE EXISTS (SELECT 1
                               FROM F_DOCENTETE_SUPPR docL
                               WHERE ISNULL(docE.DataBaseSource,'') = ISNULL(docL.DataBaseSource,'')
                               AND docE.DO_Domaine = docL.DO_Domaine
                               AND docE.DO_Type = docL.DO_Type
                               AND docE.DO_Piece = docL.DO_Piece )
                         AND NOT EXISTS (SELECT 1
                               FROM F_DOCLIGNE docL
                               WHERE ISNULL(docE.DataBaseSource,'') = ISNULL(docL.DataBaseSource,'')
                               AND docE.DO_Domaine = docL.DO_Domaine
                               AND docE.DO_Type = docL.DO_Type
                               AND docE.DO_Piece = docL.DO_Piece )
                         AND NOT EXISTS (SELECT 1
                               FROM F_DOCREGL docL
                               WHERE ISNULL(docE.DataBaseSource,'') = ISNULL(docL.DataBaseSource,'')
                               AND docE.DO_Domaine = docL.DO_Domaine
                               AND docE.DO_Type = docL.DO_Type
                               AND docE.DO_Piece = docL.DO_Piece )
                         AND NOT EXISTS (SELECT 1
                               FROM F_REGLECH docL
                               WHERE ISNULL(docE.DataBaseSource,'') = ISNULL(docL.DataBaseSource,'')
                               AND docE.DO_Domaine = docL.DO_Domaine
                               AND docE.DO_Type = docL.DO_Type
                               AND docE.DO_Piece = docL.DO_Piece )

                        """.formatted(filename) +
                /*" IF OBJECT_ID('F_DOCENTETE_SUPPR') IS NOT NULL \n" +
                " DROP TABLE F_DOCENTETE_SUPPR \n"+*/
                """
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
                           'Delete '+ ' %s',
                           'F_DOCENTETE',
                           GETDATE());
                        END CATCH\
                        """.formatted(filename);

    }
}
