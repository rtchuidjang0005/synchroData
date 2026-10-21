import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigFile {
    private JPanel panel1;
    private JCheckBox SrcArticlesCheckBox, SrcTiersCheckBox, SrcDepotCheckBox, SrcEnteteCheckBox, SrcLigneCheckBox;
    private JCheckBox SrcReglementCheckBox, SrcArtStockCheckBox, SrcEcritureCCheckBox, SrcEcritureACheckBox;
    private JCheckBox SrcTarifCondCheckBox, SrcEnumCondCheckBox, SrcPConditionnementCheckBox, SrcPUniteCheckBox;
    private JCheckBox SrcCompteGeneralCheckBox, SrcTaxeCheckBox, factureDeVenteCheckBox, devisCheckBox, bonDeLivraisonCheckBox;
    private JCheckBox factureDAchatCheckBox, documentDEntréeCheckBox, documentDeSortieCheckBox, documentDeTransfertCheckBox;
    private JCheckBox documentInterne1CheckBox, documentInterne2CheckBox, venteCheckBox, stockCheckBox, SrcJournauxCheckBox;
    private JCheckBox SrcCollaborateurCheckBox, EnvoiCheckBox, ReceptionCheckBox, ActiveCheckBox;
    private JTextField SrcServerName, SrcLogin, SrcMotDePasse, SrcRepertoire, dossierFTPSource, codeAgence, SrcNomConfig;
    private JComboBox<ComboItem> SrcListConfig;
    private JComboBox<String> SrcBaseDeDonnees;
    private JButton validerButton, SrcAjoutConfig;
    private JFormattedTextField dateMajConfig;
    private JScrollPane panelScroll;

    private final String databaseSourceFile = "resource/databaseSource.json";
    private final JSONArray infoSource = DataBase.getInfoConnexion(databaseSourceFile);
    private JSONObject object = (JSONObject) (infoSource != null ? infoSource.get(0) : null);
    private ComboItem comboItem;
    private boolean newItem;
    private boolean focusOutName;
    private boolean loadingDatabase;

    private final Map<String, JCheckBox> checkBoxMap = new HashMap<>();

    // Tables reçues : cochée = met à jour un enregistrement déjà présent, décochée = l'ignore.
    // Sauvegardé dans "updateExisting" ({"F_COMPTET":"0", ...}) ; table absente = mise à jour.
    private static final String[][] UPDATE_EXISTING_TABLES = {
            {"F_COMPTEG", "Comptes généraux"}, {"F_TAXE", "Taxes"}, {"F_JOURNAUX", "Journaux"},
            {"F_COLLABORATEUR", "Collaborateurs"}, {"F_COMPTET", "Clients / fournisseurs"}, {"F_LIVRAISON", "Adresses de livraison"},
            {"F_DEPOT", "Dépôts"}, {"F_DEPOTEMPL", "Emplacements"}, {"F_CAISSE", "Caisses"},
            {"F_FAMILLE", "Familles"}, {"F_FAMCOMPTA", "Familles compta"}, {"F_ARTICLE", "Articles"},
            {"F_ARTCLIENT", "Articles clients"}, {"F_ARTFOURNISS", "Articles fournisseurs"}, {"F_ARTCOMPTA", "Articles compta"},
            {"F_ARTSTOCK", "Stocks"}, {"F_CONDITION", "Conditionnements"}, {"F_TARIFCOND", "Tarifs cond."},
            {"F_ENUMCOND", "Énumérés cond."}, {"F_RESSOURCEPROD", "Ressources"}, {"F_ARTICLERESSOURCE", "Articles ressources"},
            {"F_DOCENTETE", "En-têtes documents"}, {"F_DOCLIGNE", "Lignes documents"}, {"F_DOCREGL", "Échéances documents"},
            {"F_CREGLEMENT", "Règlements"}, {"F_REGLECH", "Règlements / échéances"}, {"F_JMOUV", "Journaux mouvements"},
            {"F_ECRITUREC", "Écritures comptables"}, {"F_ECRITUREA", "Écritures analytiques"}};
    private final Map<String, JCheckBox> updateExistingMap = new LinkedHashMap<>();

    public ConfigFile() {
        // Saisie libre possible, la liste des bases est chargée depuis le serveur à l'ouverture
        SrcBaseDeDonnees.setEditable(true);
        initCheckBoxMap();
        JPanel updateExistingPanel = createUpdateExistingPanel();
        initValue();

        // Créer un panneau avec le défilement
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS)); // Ajuster la disposition si nécessaire
        addBeforeValidateButton(updateExistingPanel);

        JScrollPane scrollPane = new JScrollPane(panel1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        validerButton.addActionListener(e -> saveConfiguration());
        SrcAjoutConfig.addActionListener(e -> addNewConfig());

        for (JCheckBox checkBox : checkBoxMap.values()) {
            checkBox.addActionListener(e -> updateConfigData());
        }

        addListeners(SrcServerName, SrcLogin, SrcMotDePasse, SrcRepertoire, dossierFTPSource, codeAgence);
        addListeners((JTextField) SrcBaseDeDonnees.getEditor().getEditorComponent());
        SrcBaseDeDonnees.addActionListener(e -> {
            if (!loadingDatabase) updateConfigData();
        });
        SrcBaseDeDonnees.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                loadDatabases();
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });

        SrcRepertoire.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileChooser();
            }
        });

        SrcNomConfig.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateConfigData();
                setListConfig(false);
            }
        });

        SrcListConfig.addActionListener(e -> {
            if (!focusOutName) {
                comboItem = (ComboItem) SrcListConfig.getSelectedItem();
                if (!newItem) {
                    getConfigData(Integer.parseInt(comboItem.getValue()));
                }
            }
        });

        addGroupedCheckBoxListener(venteCheckBox, factureDeVenteCheckBox, devisCheckBox, bonDeLivraisonCheckBox);
        addGroupedCheckBoxListener(stockCheckBox, documentDEntréeCheckBox, documentDeSortieCheckBox, documentDeTransfertCheckBox);
        addMutuallyExclusiveCheckBoxListener(EnvoiCheckBox, ReceptionCheckBox);
    }

    private void initCheckBoxMap() {
        checkBoxMap.put("article", SrcArticlesCheckBox);
        checkBoxMap.put("tiers", SrcTiersCheckBox);
        checkBoxMap.put("depot", SrcDepotCheckBox);
        checkBoxMap.put("entete", SrcEnteteCheckBox);
        checkBoxMap.put("ligne", SrcLigneCheckBox);
        checkBoxMap.put("reglement", SrcReglementCheckBox);
        checkBoxMap.put("artstock", SrcArtStockCheckBox);
        checkBoxMap.put("ecriturec", SrcEcritureCCheckBox);
        checkBoxMap.put("ecriturea", SrcEcritureACheckBox);
        checkBoxMap.put("compteg", SrcCompteGeneralCheckBox);
        checkBoxMap.put("taxe", SrcTaxeCheckBox);
        checkBoxMap.put("journaux", SrcJournauxCheckBox);
        checkBoxMap.put("collaborateur", SrcCollaborateurCheckBox);
        checkBoxMap.put("envoi", EnvoiCheckBox);
        checkBoxMap.put("active", ActiveCheckBox);
        checkBoxMap.put("reception", ReceptionCheckBox);
    }

    private JPanel createUpdateExistingPanel() {
        JPanel panel = new JPanel(new java.awt.GridLayout(0, 3, 8, 2));
        panel.setBorder(BorderFactory.createTitledBorder(
                "Réception : mettre à jour si l'enregistrement existe déjà (décoché = ignorer)"));
        for (String[] table : UPDATE_EXISTING_TABLES) {
            JCheckBox checkBox = new JCheckBox(table[1], true);
            checkBox.setToolTipText(table[0]);
            checkBox.addActionListener(e -> updateConfigData());
            updateExistingMap.put(table[0], checkBox);
            panel.add(checkBox);
        }
        return panel;
    }

    // Insère le bloc dans panel1 juste avant la zone qui contient le bouton Valider
    private void addBeforeValidateButton(JComponent component) {
        java.awt.Component child = validerButton;
        while (child.getParent() != null && child.getParent() != panel1) {
            child = child.getParent();
        }
        int index = child.getParent() == panel1 ? panel1.getComponentZOrder(child) : panel1.getComponentCount();
        component.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel1.add(component, index);
    }

    private void fileChooser() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        if (!SrcRepertoire.getText().equals("")) {
            chooser.setCurrentDirectory(new File(SrcRepertoire.getText()));
        }
        chooser.setDialogTitle("Choisissez un répertoire:");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showSaveDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().isDirectory()) {
                SrcRepertoire.setText(chooser.getSelectedFile().toString());
            }
        }
    }

    private String getDatabaseName() {
        Object item = SrcBaseDeDonnees.isEditable() ? SrcBaseDeDonnees.getEditor().getItem() : SrcBaseDeDonnees.getSelectedItem();
        return item == null ? "" : item.toString().trim();
    }

    private void setDatabaseName(String name) {
        loadingDatabase = true;
        SrcBaseDeDonnees.setSelectedItem(name == null ? "" : name);
        loadingDatabase = false;
    }

    // Liste les bases utilisateur du serveur avec le login / mot de passe saisis
    private void loadDatabases() {
        if (SrcServerName.getText().isEmpty())
            return;
        String current = getDatabaseName();
        String dbURL = Table.jdbcUrl(SrcServerName.getText(), "master;loginTimeout=5");
        java.util.Properties properties = new java.util.Properties();
        properties.put("user", SrcLogin.getText());
        properties.put("password", SrcMotDePasse.getText());
        properties.put("encrypt", "false");
        try (java.sql.Connection con = java.sql.DriverManager.getConnection(dbURL, properties);
             java.sql.ResultSet rs = con.createStatement().executeQuery(
                     "SELECT name FROM sys.databases WHERE database_id > 4 AND state = 0 ORDER BY name")) {
            loadingDatabase = true;
            SrcBaseDeDonnees.removeAllItems();
            while (rs.next())
                SrcBaseDeDonnees.addItem(rs.getString(1));
            loadingDatabase = false;
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(panel1, "Impossible de lister les bases :\n" + ex.getMessage(),
                    "Base de données", JOptionPane.WARNING_MESSAGE);
        }
        setDatabaseName(current);
    }

    private void initValue() {
        newItem = false;
        setListConfig(false);
        getConfigData(0);
        comboItem = SrcListConfig.getItemAt(0);
    }

    private void setListConfig(boolean ajout) {
        int selected = (SrcListConfig.getSelectedIndex() == -1) ? 0 : SrcListConfig.getSelectedIndex();
        if (ajout && infoSource != null) {
            selected = infoSource.size() - 1;
        }
        SrcListConfig.validate();
        if (infoSource != null) {
            for (int i = 0; i < infoSource.size(); i++) {
                JSONObject object = (JSONObject) infoSource.get(i);
                String nomConfig = (String) object.get("nomconfig");
                ComboItem comboItemAdd = new ComboItem(nomConfig, String.valueOf(i));
                if (i == selected) comboItem = comboItemAdd;
                SrcListConfig.addItem(comboItemAdd);
            }
        }
        SrcListConfig.setSelectedIndex(selected);
    }

    private void addNewConfig() {
        if (infoSource != null) {
            newItem = true;
            infoSource.add(initJson());
            addItemToList();
            setListConfig(true);
            getConfigData(infoSource.size() - 1);
            newItem = false;
        }
    }

    private void addItemToList() {
        if (infoSource != null) {
            SrcListConfig.addItem(new ComboItem("", String.valueOf(infoSource.size() - 1)));
        }
    }

    private JSONObject initJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("servername", "");
        jsonObject.put("nomconfig", "");
        jsonObject.put("database", "");
        jsonObject.put("username", "");
        jsonObject.put("password", "");
        jsonObject.put("path", "");
        jsonObject.put("folderftp", "");
        jsonObject.put("codeagence", "");
        jsonObject.put("article", "0");
        jsonObject.put("tiers", "0");
        jsonObject.put("depot", "0");
        jsonObject.put("entete", "0");
        jsonObject.put("ligne", "0");
        jsonObject.put("reglement", "0");
        jsonObject.put("artstock", "0");
        jsonObject.put("ecriturec", "0");
        jsonObject.put("ecriturea", "0");
        jsonObject.put("compteg", "0");
        jsonObject.put("taxe", "0");
        jsonObject.put("journaux", "0");
        jsonObject.put("collaborateur", "0");
        jsonObject.put("envoi", "0");
        jsonObject.put("active", "1");
        jsonObject.put("reception", "0");
        JSONObject typeDocument = new JSONObject();
        typeDocument.put("facturedevente", "0");
        typeDocument.put("devis", "0");
        typeDocument.put("bondelivraison", "0");
        typeDocument.put("facturedachat", "0");
        typeDocument.put("entree", "0");
        typeDocument.put("sortie", "0");
        typeDocument.put("transfert", "0");
        typeDocument.put("documentinterne1", "0");
        typeDocument.put("documentinterne2", "0");
        jsonObject.put("typedocument", typeDocument);
        return jsonObject;
    }

    private JSONObject updateJson(JSONObject jsonObject) {
        jsonObject.put("servername", SrcServerName.getText());
        jsonObject.put("nomconfig", SrcNomConfig.getText());
        jsonObject.put("database", getDatabaseName());
        jsonObject.put("username", SrcLogin.getText());
        jsonObject.put("password", SrcMotDePasse.getText());
        jsonObject.put("path", SrcRepertoire.getText());
        jsonObject.put("folderftp", dossierFTPSource.getText());
        jsonObject.put("codeagence", codeAgence.getText());
        jsonObject.put("article", checkBoxMap.get("article").isSelected() ? "1" : "0");
        jsonObject.put("tiers", checkBoxMap.get("tiers").isSelected() ? "1" : "0");
        jsonObject.put("depot", checkBoxMap.get("depot").isSelected() ? "1" : "0");
        jsonObject.put("entete", checkBoxMap.get("entete").isSelected() ? "1" : "0");
        jsonObject.put("ligne", checkBoxMap.get("ligne").isSelected() ? "1" : "0");
        jsonObject.put("reglement", checkBoxMap.get("reglement").isSelected() ? "1" : "0");
        jsonObject.put("artstock", checkBoxMap.get("artstock").isSelected() ? "1" : "0");
        jsonObject.put("ecriturec", checkBoxMap.get("ecriturec").isSelected() ? "1" : "0");
        jsonObject.put("ecriturea", checkBoxMap.get("ecriturea").isSelected() ? "1" : "0");
        jsonObject.put("compteg", checkBoxMap.get("compteg").isSelected() ? "1" : "0");
        jsonObject.put("taxe", checkBoxMap.get("taxe").isSelected() ? "1" : "0");
        jsonObject.put("journaux", checkBoxMap.get("journaux").isSelected() ? "1" : "0");
        jsonObject.put("collaborateur", checkBoxMap.get("collaborateur").isSelected() ? "1" : "0");
        jsonObject.put("envoi", checkBoxMap.get("envoi").isSelected() ? "1" : "0");
        jsonObject.put("active", checkBoxMap.get("active").isSelected() ? "1" : "0");
        jsonObject.put("reception", checkBoxMap.get("reception").isSelected() ? "1" : "0");

        JSONObject typeDocument = (JSONObject) jsonObject.get("typedocument");
        typeDocument.put("facturedevente", factureDeVenteCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("devis", devisCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("bondelivraison", bonDeLivraisonCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("facturedachat", factureDAchatCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("entree", documentDEntréeCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("sortie", documentDeSortieCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("transfert", documentDeTransfertCheckBox.isSelected() ? "1" : "0");
        typeDocument.put("documentinterne1", documentInterne1CheckBox.isSelected() ? "1" : "0");
        typeDocument.put("documentinterne2", documentInterne2CheckBox.isSelected() ? "1" : "0");

        JSONObject updateExisting = new JSONObject();
        for (Map.Entry<String, JCheckBox> entry : updateExistingMap.entrySet()) {
            updateExisting.put(entry.getKey(), entry.getValue().isSelected() ? "1" : "0");
        }
        jsonObject.put("updateExisting", updateExisting);
        return jsonObject;
    }

    private void getConfigData(int index) {
        object = (JSONObject) infoSource.get(index);
        SrcServerName.setText((String) object.get("servername"));
        SrcNomConfig.setText((String) object.get("nomconfig"));
        setDatabaseName((String) object.get("database"));
        SrcLogin.setText((String) object.get("username"));
        SrcMotDePasse.setText((String) object.get("password"));
        SrcRepertoire.setText((String) object.get("path"));
        dossierFTPSource.setText((String) object.get("folderftp"));
        codeAgence.setText((String) object.get("codeagence"));

        updateCheckBoxSelection("article", "tiers", "depot", "entete", "ligne", "reglement", "artstock",
                "ecriturec", "ecriturea", "compteg", "taxe", "journaux", "collaborateur", "envoi", "active", "reception");

        JSONObject typeDocument = (JSONObject) object.get("typedocument");
        factureDeVenteCheckBox.setSelected("1".equals(typeDocument.get("facturedevente")));
        devisCheckBox.setSelected("1".equals(typeDocument.get("devis")));
        bonDeLivraisonCheckBox.setSelected("1".equals(typeDocument.get("bondelivraison")));
        factureDAchatCheckBox.setSelected("1".equals(typeDocument.get("facturedachat")));
        documentDEntréeCheckBox.setSelected("1".equals(typeDocument.get("entree")));
        documentDeSortieCheckBox.setSelected("1".equals(typeDocument.get("sortie")));
        documentDeTransfertCheckBox.setSelected("1".equals(typeDocument.get("transfert")));
        documentInterne1CheckBox.setSelected("1".equals(typeDocument.get("documentinterne1")));
        documentInterne2CheckBox.setSelected("1".equals(typeDocument.get("documentinterne2")));

        // table absente du paramètre = mise à jour (comportement historique)
        Object updateExisting = object.get("updateExisting");
        for (Map.Entry<String, JCheckBox> entry : updateExistingMap.entrySet()) {
            Object value = updateExisting instanceof JSONObject ? ((JSONObject) updateExisting).get(entry.getKey()) : null;
            entry.getValue().setSelected(!"0".equals(value));
        }

        // Update dateMajConfig
        if (object.containsKey("datemaj")) {
            String dateMajStr = (String) object.get("datemaj");
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(dateMajStr);
                dateMajConfig.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dateMajConfig.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()));
        }
    }

    private void updateCheckBoxSelection(String... keys) {
        for (String key : keys) {
            JCheckBox checkBox = checkBoxMap.get(key);
            checkBox.setSelected("1".equals(object.get(key)));
        }
    }

    private void saveConfiguration() {
        if (comboItem != null) {
            updateConfigData();
            // Mettre à jour la date de modification
            String dateAffichee = dateMajConfig.getText();
            DateTimeFormatter formatterAffichee = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date;
            try {
                date = LocalDate.parse(dateAffichee, formatterAffichee);
            } catch (java.time.format.DateTimeParseException e) {
                JOptionPane.showMessageDialog(panel1, "Date de mise à jour invalide : « " + dateAffichee + " ».\nFormat attendu : jj/mm/aaaa.",
                        "Configuration non enregistrée", JOptionPane.ERROR_MESSAGE);
                return;
            }
            DateTimeFormatter formatterJSON = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String datePourJSON = date.format(formatterJSON);
            object.put("datemaj", datePourJSON);

            try {
                DataBase.saveInfoConnexion(infoSource, databaseSourceFile);
                JOptionPane.showMessageDialog(panel1, "Configuration « " + SrcNomConfig.getText() + " » enregistrée.",
                        "Configuration", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(panel1, "Impossible d'écrire " + new File(databaseSourceFile).getAbsolutePath()
                        + " :\n" + e.getMessage(), "Configuration non enregistrée", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addListeners(JTextField... textFields) {
        for (JTextField textField : textFields) {
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateConfigData();
                }
            });
        }
    }

    private void addGroupedCheckBoxListener(JCheckBox masterCheckBox, JCheckBox... groupedCheckBoxes) {
        masterCheckBox.addItemListener(e -> {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            for (JCheckBox checkBox : groupedCheckBoxes) {
                checkBox.setSelected(selected);
            }
            updateConfigData();
        });
    }

    private void addMutuallyExclusiveCheckBoxListener(JCheckBox checkBox1, JCheckBox checkBox2) {
        ItemListener listener = e -> {
            if (e.getSource() == checkBox1 && checkBox1.isSelected()) {
                checkBox2.setSelected(false);
            } else if (e.getSource() == checkBox2 && checkBox2.isSelected()) {
                checkBox1.setSelected(false);
            }
            updateConfigData();
        };
        checkBox1.addItemListener(listener);
        checkBox2.addItemListener(listener);
    }


    private void updateConfigData() {
        if (comboItem != null && object != null) {
            object = updateJson(object);
            infoSource.set(Integer.parseInt(comboItem.getValue()), object);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ConfigFile");
        ConfigFile configFile = new ConfigFile();

        // Ajouter le panneau défilant
        JScrollPane scrollPane = new JScrollPane(configFile.panel1);
        frame.setContentPane(scrollPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void setTaxe(int valSelect){
        if(valSelect == 1){
            SrcCompteGeneralCheckBox.setSelected(true);
            SrcTaxeCheckBox.setSelected(true);
        }else{
            SrcTaxeCheckBox.setSelected(false);
        }
    }

    public void setJournaux(int valSelect){
        if(valSelect == 1){
            SrcCompteGeneralCheckBox.setSelected(true);
            SrcJournauxCheckBox.setSelected(true);
        }else{
            SrcJournauxCheckBox.setSelected(false);
        }
    }

    public void setCompteTiers(int valSelect){
        if(valSelect == 1){
            SrcCompteGeneralCheckBox.setSelected(true);
            SrcTiersCheckBox.setSelected(true);
        }else{
            SrcTiersCheckBox.setSelected(false);
        }
    }

    public void setArticle(int valSelect){
        SrcArticlesCheckBox.setSelected(valSelect == 1);
    }

    public void setArtStock(int valSelect){
        if(valSelect == 1){
            setArticle(1);
            SrcDepotCheckBox.setSelected(true);
        }else{
            setArticle(0);
            SrcDepotCheckBox.setSelected(false);
        }
    }

    public void setEntete(int valSelect){
        if(valSelect == 1){
            setArtStock(1);
            setCompteTiers(1);
            setJournaux(1);
            setTaxe(1);
        }else{
            setArticle(0);
            setCompteTiers(0);
            setJournaux(0);
            setTaxe(0);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
    public class DataBase {
        public static JSONArray getInfoConnexion(String databaseSourceFile) {
            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(databaseSourceFile)) {
                // Lire le fichier JSON et le parser en JSONArray
                Object obj = jsonParser.parse(reader);
                return (JSONArray) obj;
            } catch (IOException e) {
                e.printStackTrace();
                return new JSONArray(); // Retourne un tableau vide en cas d'erreur
            } catch (org.json.simple.parser.ParseException e) {
                throw new RuntimeException(e);
            }
        }
        public static void saveInfoConnexion(JSONArray infoSource, String databaseSourceFile) throws IOException {
            try (FileWriter file = new FileWriter(databaseSourceFile)) {
                file.write(infoSource.toJSONString());
                file.flush();
            }
        }
    }
    static class ComboItem
    {
        private final String key;
        private final String value;

        public ComboItem(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }
    }

}
