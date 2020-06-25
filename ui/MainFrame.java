package ui;
import javax.swing.*;
import db.DBConnection;
import mdlaf.animation.*;
import mdlaf.utils.MaterialColors;
import net.miginfocom.swing.MigLayout;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import net.sourceforge.jdatepicker.impl.*;
import java.time.LocalDate;
import java.text.SimpleDateFormat;

// TODO : Refactor the whole class
public class MainFrame extends JFrame{
    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rs = null;

    public MainFrame() {
        super("MoFin");
        this.con = DBConnection.getConnection();
        setMinimumSize(new Dimension (600, 400));
        initGUI();
    } 

    public void onSelectedTableRow()
    {
        int idx = this.table.getSelectedRow();
        if (idx == -1) return;
        try {
            idTransaction = (String) tableModel.getValueAt(idx, 0);
            Date selectedDate = new SimpleDateFormat("yyyy-MM-dd").parse((String) tableModel.getValueAt(idx,1));
            dateModel.setValue(selectedDate);
            categoryList.setSelectedIndex(getCategoryIndexFromDB((String) tableModel.getValueAt(idx, 2)));
            amountField.setText((String) tableModel.getValueAt(idx, 3));
            descriptionField.setText((String) tableModel.getValueAt(idx, 4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTableData()
    {
        tableModel.getDataVector().removeAllElements();
        tableModel.fireTableDataChanged();
        try {
            stmt = con.createStatement();
            String sql = "SELECT * FROM transactions";
            rs = stmt.executeQuery(sql);

            while(rs.next())
            {
                Object[] o = new Object[5];
                o[0] = rs.getString("id");
                o[1] = rs.getString("date");
                o[2] = rs.getString("category");
                o[3] = rs.getString("amount");
                o[4] = rs.getString("description");
                tableModel.addRow(o);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initGUI()
    {    
        JPanel navPanel = new JPanel(new GridLayout(1,1));
 
        JTabbedPane tabbedPane = new JTabbedPane();

        // initializing every page components
        JComponent transactionPanel = makeTransactionPanel();
        JComponent settingsPanel = makeSettingsPage();

        // add every panel into tabbedPane
        
        tabbedPane.addTab("Transaction", transactionPanel);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Settings", settingsPanel);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        navPanel.add(tabbedPane);

		// JPanel content = new JPanel ();
        // content.add (button);
		// frame.getContentPane().add(content, BorderLayout.CENTER);


        add(navPanel);
		pack();
        setVisible (true);
        getTableData();
        // Implements ActionListener on UI-elements
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                onSelectedTableRow();
            }
          });
        
        addButton.addActionListener(new ActionListener() 
        {
           @Override
           public void actionPerformed(ActionEvent e)
           {
                if("Add".equals(addButton.getText()))
                {
                    addButton.setText("Save");
                    editButton.setText("Cancel");
                    deleteButton.setEnabled(false);
                    refreshButton.setEnabled(false);
                    idTransaction = "";
                    resetDateModel();
                }
                else if("Update".equals(addButton.getText()))
                {
                    String sql = "UPDATE transactions SET date = ?, category = ?, amount = ?, description = ? WHERE id = ?";
                    try {
                        PreparedStatement p2 = con.prepareStatement(sql);
                        Date datee = (Date) datePicker.getModel().getValue();
                        p2.setDate(1, new java.sql.Date(datee.getTime()));
                        p2.setString(2, (String) categoryList.getSelectedItem());
                        p2.setInt(3, Integer.parseInt(amountField.getText()));
                        p2.setString(4, descriptionField.getText());
                        p2.setString(5, idTransaction);
                        p2.executeUpdate();
                        p2.close();
                        JOptionPane.showMessageDialog(null, "Data berhasil diubah");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Terjadi Kesalahan " + ex.getMessage());
                    }
                    addButton.setText("Add");
                    editButton.setText("Edit");
                    amountField.setText("");
                    descriptionField.setText("");
                    deleteButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                    getTableData();
                    updateBalance();
                }
                else
                {
                    String sql = "INSERT INTO transactions (date, category, amount, description) values(?,?,?,?)";
                    try {
                        PreparedStatement p2 = con.prepareStatement(sql);
                        Date datee = (Date) datePicker.getModel().getValue();
                        p2.setDate(1, new java.sql.Date(datee.getTime()));
                        p2.setString(2, (String) categoryList.getSelectedItem());
                        p2.setInt(3, Integer.parseInt(amountField.getText()));
                        p2.setString(4, descriptionField.getText());
                        p2.executeUpdate();
                        p2.close();
                        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Terjadi Kesalahan " + ex.getMessage());
                    }
                    addButton.setText("Add");
                    editButton.setText("Edit");
                    amountField.setText("");
                    descriptionField.setText("");
                    deleteButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                    getTableData();
                    updateBalance();
                }
           } 
        });

        editButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if("Edit".equals(editButton.getText()))
                {
                    addButton.setText("Update");
                    editButton.setText("Cancel");
                    deleteButton.setEnabled(false);
                    refreshButton.setEnabled(false);
                }
                else{
                    addButton.setText("Add");
                    editButton.setText("Edit");
                    deleteButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "DELETE FROM transactions WHERE id = ?";
                try {
                    PreparedStatement p2 = con.prepareStatement(sql);
                    p2.setString(1, idTransaction);
                    p2.executeUpdate();
                    p2.close();
                    getTableData();
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Terjadi Kesalahan " + ex.getMessage());
                }
            }
        });

        refreshButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                getTableData();
                idTransaction = "";
                resetDateModel();
                categoryList.setSelectedIndex(0);
                amountField.setText("");  
                descriptionField.setText("");
            }

        });

        saveSettingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try {

                    String sql = "INSERT INTO user (id, name, currentbalance, initialbalance) VALUES(? , ?, ?, ?) ON DUPLICATE KEY UPDATE name=?,initialbalance=?,currentbalance=?";
                    PreparedStatement p2 = con.prepareStatement(sql);
                    p2.setString(1, "0");
                    p2.setString(2, nameField.getText());
                    p2.setInt(3, Integer.parseInt(currentBalanceString.getText()));
                    p2.setInt(4, Integer.parseInt(initialBalanceField.getText()));
                    p2.setString(5, nameField.getText());
                    p2.setInt(6, Integer.parseInt(currentBalanceString.getText()));
                    p2.setInt(7, Integer.parseInt(initialBalanceField.getText()));
                    p2.executeUpdate();
                    p2.close();
                    getSettingsData();
                    updateBalance();
                    JOptionPane.showMessageDialog(null, "Updated Settings!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        cancelSettingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getSettingsData();
            }
        });
    }

    public int getCategoryIndexFromDB(String category)
    {
        if (category == null || category.equals("Income")) {
            return 0;
        } else {
            return 1;
        }
    }

    public JComponent makeTransactionPanel()
    {
        formPanel.add(titleLabel, "center, wrap, span");
        // Pembuatan Panel form input data user
        formPanel.add(currentBalanceLabel, "gapy 20");
        formPanel.add(currentBalanceString, "wrap, push, span");
        formPanel.add(dateLabel, "gapy 5");
        formPanel.add(datePicker, "wrap, push, span");
        formPanel.add(categoryLabel);
        formPanel.add(categoryList, "wrap, push, span");
        formPanel.add(amountLabel);
        formPanel.add(amountField, "wrap, push, span");
        formPanel.add(descriptionLabel);
        formPanel.add(descriptionField, "wrap, push, span");

        // Deklarasi model table dengan 4 kolom
        tableModel.addColumn("id");
        tableModel.addColumn("Date");
        tableModel.addColumn("Category");
        tableModel.addColumn("Amount");
        tableModel.addColumn("Description");
        
        formPanel.add(new JScrollPane(table), "wrap, width 100%, growx, push, span, gapy 20");


        // Deklarasi buttton-button dan panel untuk button
        JPanel buttonPanel = new JPanel();

        addButton.setMaximumSize (new Dimension (200, 200));
        addButton.setBackground(MaterialColors.LIGHT_BLUE_400);
        addButton.setForeground(MaterialColors.WHITE);
        addButton.setOpaque(true);
        addButton.addMouseListener(MaterialUIMovement.getMovement(addButton, MaterialColors.LIGHT_BLUE_600));

        editButton.setMaximumSize (new Dimension (200, 200));
        editButton.setBackground(MaterialColors.AMBER_400);
        editButton.setForeground(MaterialColors.WHITE);
        editButton.setOpaque(true);
        editButton.addMouseListener(MaterialUIMovement.getMovement(editButton, MaterialColors.AMBER_600));
        
        deleteButton.setMaximumSize (new Dimension (200, 200));
        deleteButton.setBackground(MaterialColors.RED_400);
        deleteButton.setForeground(MaterialColors.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.addMouseListener(MaterialUIMovement.getMovement(deleteButton, MaterialColors.RED_600));

        refreshButton.setMaximumSize (new Dimension (200, 200));
        
        //tambahkan button pada panel
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        formPanel.add(buttonPanel, "gapy 10, center, span");
        return formPanel;
    }

    public JComponent makeDummyPanel()
    {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel("Dum dum dummy");
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1,1));
        panel.add(filler);
        return panel;
    }

    public void updateBalance()
    {
        getSettingsData();
        int incomeBalance = 0;
        int expenseBalance = 0;
        int initialBalanceValue;
        System.out.println();
        if("".equals(initialBalanceField.getText()))
        {
            initialBalanceValue = 0;
        }
        else
        {
            initialBalanceValue = Integer.parseInt(initialBalanceField.getText());
        }
        int currentBalanceValue = Integer.parseInt(currentBalanceString.getText());
        int currentCashFlow;
        String sql = "SELECT SUM(amount) as balance from transactions WHERE category=?";
        try {
            PreparedStatement p2 = con.prepareStatement(sql);
            Date datee = (Date) datePicker.getModel().getValue();
            p2.setString(1, "Income");
            rs = p2.executeQuery();
            while(rs.next())
            {
                incomeBalance = rs.getInt("balance");
            }
            p2.setString(1, "Expense");
            rs = p2.executeQuery();
            while(rs.next())
            {
                expenseBalance = rs.getInt("balance");
            }

            currentCashFlow = incomeBalance - expenseBalance;
            currentBalanceValue = initialBalanceValue + currentCashFlow;
            currentBalanceString.setText(String.valueOf(currentBalanceValue));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Terjadi Kesalahan " + ex.getMessage());
        }
    }

    public JComponent makeSettingsPage()
    {
        getSettingsData();
        updateBalance();
        // TODO : Refactor the settings page
        JPanel settingsPanel = new JPanel(new MigLayout());
        JLabel settingsTitle = new JLabel("Settings");
        settingsPanel.add(settingsTitle, "center, wrap, span");
        settingsPanel.add(nameLabel);
        settingsPanel.add(nameField, "wrap, push, span");
        settingsPanel.add(initialBalanceLabel);
        settingsPanel.add(initialBalanceField, "wrap, push, span");
        
        JPanel settingsButtonPanel = new JPanel();
        settingsButtonPanel.add(saveSettingsButton);
        settingsButtonPanel.add(cancelSettingsButton);
        settingsPanel.add(settingsButtonPanel);
        return settingsPanel;
    }

    public void getSettingsData()
    {
        try {
            stmt = con.createStatement();
            String sql = "SELECT * FROM user";
            rs = stmt.executeQuery(sql);

            while(rs.next())
            {
                nameField.setText(rs.getString("name"));
                System.out.println(rs.getString("initialBalance"));
                initialBalanceField.setText(rs.getString("initialbalance"));
                currentBalanceString.setText(rs.getString("currentbalance"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JComponent makeErrorPanel()
    {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel("Oh no! the page you're trying to open does not exists :(");
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1,1));
        panel.add(filler);
        return panel;
    }

    public void resetDateModel()
    {
        LocalDate now = LocalDate.now();
        dateModel.setDate(now.getYear(), now.getMonthValue()-1, now.getDayOfMonth());
        dateModel.setSelected(true);
    }

    // Class-wide ui-elements declaration

    private String idTransaction;
    private JPanel formPanel = new JPanel(new MigLayout());
    private JLabel titleLabel = new JLabel("Insert your transactions here");
    private JLabel categoryLabel = new JLabel("Category");
    private String[] categoryStrings = {"Income", "Expense"};
    private JComboBox categoryList = new JComboBox(categoryStrings);

    private JLabel dateLabel = new JLabel("Date");
    private UtilDateModel dateModel = new UtilDateModel();
    private JDatePanelImpl datePanel = new JDatePanelImpl(dateModel);
    private JDatePickerImpl datePicker = new JDatePickerImpl(datePanel);

    private JLabel amountLabel = new JLabel("Amount");
    private JTextField amountField = new JTextField(50);
    private JLabel descriptionLabel = new JLabel("description");
    private JTextField descriptionField = new JTextField(50);

    private JButton addButton = new JButton("Add");
    private JButton editButton = new JButton("Edit");
    private JButton deleteButton = new JButton("Delete");
    private JButton refreshButton = new JButton("Refresh");
    private DefaultTableModel tableModel = new DefaultTableModel();
    private JTable table = new JTable(tableModel);

    private JLabel currentBalanceLabel = new JLabel("Current Balance");
    private JLabel currentBalanceString = new JLabel("0");
    private JLabel initialBalanceLabel = new JLabel("Initial Balance");
    private JTextField initialBalanceField = new JTextField(50);
    private JLabel nameLabel = new JLabel("name");
    private JTextField nameField = new JTextField(50);
    private JButton saveSettingsButton = new JButton("Save");
    private JButton cancelSettingsButton = new JButton("Cancel");
    // end of declaration
}