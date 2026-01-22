import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MyFrame extends JFrame {

    private String TabelCurent;
    private String UltimTabel;
    private JComboBox<String> Coloane = new JComboBox<>();
    private JComboBox<String> Cerinte = new JComboBox<>();
    private  JButton btnUtil = new JButton("Utilizatori");
    private JButton btnArticole = new JButton("Articole");
    private  JButton btnComenzi = new JButton("Comenzi");
    private JButton afisare = new JButton("Afisare Comanda");
    private JButton btnFunctie = new JButton("AplicaDiscount");
    private JButton btnDetalii = new JButton("Detalii Comenzi");
    private JButton btnCategorii = new JButton("Categorii");
    private JTextField txtCautare = new JTextField(15);
    private JButton btnCauta = new JButton("Cauta");


    MyFrame() {
        //asta e practic unde punem chestiile fereastra
        this.setTitle("Magazin de Articole Sportive");
        this.setSize(800, 600); //x si y
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //cand apasam x se inchide si se oprestre program
        this.getContentPane().setBackground(new Color(245, 245, 245));

        JPanel panelButoane = PanelButoane();


      //pentru a face afisa un tabel crea prima data un model de tabel gol
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);


//        trebuie folosit JScrollPane pt a vedea tot tabelul,altfel nu vedem headerul
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelCautare = new JPanel();
        panelCautare.setBackground(Color.WHITE);
        panelCautare.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                "Filtrare / Cautare",
                0, 0, new Font("Segoe UI", Font.BOLD, 12)
        ));

        btnCauta.setBackground(new Color(41, 128, 185));
        btnCauta.setForeground(Color.WHITE);
        btnCauta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCauta.setFocusPainted(false);

        panelCautare.add(txtCautare);
        panelCautare.add(btnCauta);


        btnCauta.addActionListener(e -> {
            // Verificam daca avem un tabel selectat
            if (TabelCurent == null || TabelCurent.isEmpty()){
                return;
            }

            String textCautat = txtCautare.getText().trim();
            String coloanaSelectata = (String) Coloane.getSelectedItem();

            // Daca nu e selectata nicio coloana sau textul e gol, reafisam tot tabelul
            if (coloanaSelectata == null || textCautat.isEmpty()) {
                AfisareTabelFiltre(model, "select * from " + TabelCurent);
                return;
            }

            // Construim query-ul
            // ILIKE pentru a ignora litere mari/mici
            // folosim ::text pentru a transforma orice coloana (chiar si numere/date) in text pentru cautare
            String sql = "select * from " + TabelCurent +
                    " where " + coloanaSelectata + "::text ILIKE '%" + textCautat + "%'";

            AfisareTabelFiltre(model, sql);
        });

        this.add(scrollPane, BorderLayout.CENTER);
        btnFunctie.addActionListener(e -> {

            int rand = table.getSelectedRow();
            if (rand == -1 || !"comandaonline".equals(TabelCurent)) {
                JOptionPane.showMessageDialog(this, "selecteaza o comanda din tabelul comenzi!");
                return;
            }

            // luam id-ul comenzii
            int idComanda = Integer.parseInt(model.getValueAt(rand, 0).toString());

            // cerem discountul de la utilizator
            String input = JOptionPane.showInputDialog(this, "introdu discountul:");
            if (input == null) return; // cancel

            int discount;
            try {
                discount = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "discount invalid!");
                return;
            }

            if (discount < 0 || discount > 100) {
                JOptionPane.showMessageDialog(this, "discountul trebuie sa fie intre 0 si 100!");
                return;
            }

            try (Connection conn = PostgresConn.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("select aplica_discount_comanda(?, ?)")) {

                stmt.setInt(1, idComanda);
                stmt.setInt(2, discount);
                stmt.execute();

                JOptionPane.showMessageDialog(this, "discount aplicat cu succes!");

                // refresh tabel comenzi
                AfisareTabelFiltre(model, "select * from comandaonline");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "eroare: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        btnUtil.addActionListener(e -> {
            TabelCurent = "utilizator";
            AfisareTabelFiltre(model, "select * from utilizator");
        });

        btnArticole.addActionListener(e -> {
            TabelCurent = "articol";
            AfisareTabelFiltre(model, "select * from articol");
        });

        btnComenzi.addActionListener(e -> {
            TabelCurent = "comandaonline";
            AfisareTabelFiltre(model, "select * from comandaonline");
        });
        btnDetalii.addActionListener(e -> {
            TabelCurent = "detaliucomanda";
            AfisareTabelFiltre(model, "select * from detaliucomanda");
        });

        btnCategorii.addActionListener(e -> {
            TabelCurent = "categoriearticol";
            AfisareTabelFiltre(model, "select * from categoriearticol");
        });

        btnDetalii.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDetalii.setBackground(new Color(236, 240, 241));
        btnDetalii.setFocusPainted(false);

        btnCategorii.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCategorii.setBackground(new Color(236, 240, 241));
        btnCategorii.setFocusPainted(false);

        //Panel pentru sortare
        JPanel panelSortare = new JPanel();
        panelSortare.setBackground(Color.WHITE);
        panelSortare.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                "Sortare",
                0, 0, new Font("Segoe UI", Font.BOLD, 12)
        ));

        Coloane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelSortare.add(Coloane);

        JButton btnCrescator = new JButton("Crescator");
        JButton btnDescrescator = new JButton("Descrescator");

        btnCrescator.setBackground(new Color(46, 204, 113));
        btnCrescator.setForeground(Color.WHITE);
        btnCrescator.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnDescrescator.setBackground(new Color(231, 76, 60));
        btnDescrescator.setForeground(Color.WHITE);
        btnDescrescator.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnCrescator.addActionListener(e -> {
            if(TabelCurent.equals("utilizator")) {
                String coloana = (String) Coloane.getSelectedItem();
                AfisareTabelFiltre(model, "select * from utilizator order by "+ coloana +" ASC");
            }
            if(TabelCurent.equals("articol")) {
                String coloana = (String) Coloane.getSelectedItem();
                AfisareTabelFiltre(model, "select * from articol order by "+ coloana +" ASC");
            }
            if(TabelCurent.equals("comandaonline")) {
                String coloana = (String) Coloane.getSelectedItem();
                AfisareTabelFiltre(model, "select * from comandaonline order by "+ coloana +" ASC");
            }
        });

        btnDescrescator.addActionListener(e -> {
            if(TabelCurent.equals("utilizator")) {
                String coloana = (String) Coloane.getSelectedItem();
                AfisareTabelFiltre(model, "select * from utilizator order by "+ coloana +" DESC");
            }
            if(TabelCurent.equals("articol")) {
                String coloana = (String) Coloane.getSelectedItem();
                AfisareTabelFiltre(model, "select * from articol order by "+ coloana +" DESC");
            }
            if(TabelCurent.equals("comandaonline")) {
                String coloana = (String) Coloane.getSelectedItem();
                AfisareTabelFiltre(model, "select * from comandaonline order by "+ coloana +" DESC");
            }
        });
        btnFunctie.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFunctie.setBackground(new Color(241, 196, 15)); // Un galben auriu
        btnFunctie.setForeground(Color.BLACK);
        btnFunctie.setFocusPainted(false);
        panelButoane.add(btnFunctie);
        panelSortare.add(btnCrescator);
        panelSortare.add(btnDescrescator);

       //Combinam panelurile

        JPanel panelTop = new JPanel(new GridLayout(4, 1, 3, 3));
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        panelTop.add(panelCautare);


        panelTop.add(panelSortare);

        Cerinte.addItem("3.a");
        Cerinte.addItem("3.b");
        Cerinte.addItem("4.a");
        Cerinte.addItem("4.b");
        Cerinte.addItem("5.a");
        Cerinte.addItem("5.b");
        Cerinte.addItem("6.a");
        Cerinte.addItem("6.b");
        panelTop.add(Cerinte);
        this.add(panelTop, BorderLayout.NORTH);
        afisare.addActionListener(e ->
        {
            AfisareCerinte(model);
        });
        panelTop.add(afisare);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void AfisareCerinte(DefaultTableModel model){
        String cerinta = (String) Cerinte.getSelectedItem();
        if(cerinta.equals("3.a")){
            String comanda = "select * from comandaonline where data_comanda>= current_date - 30 order by data_comanda DESC;";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("3.b")){
            String comanda = "select * from articol where pret>200 order by denumire ASC,categorie DESC;";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("4.a")){
            String comanda = "select c.id_co,nume,denumire,data_comanda " +
                    "from utilizator u join comandaonline c on c.id_u=u.id_u " +
                    "join detaliucomanda d on d.id_co = c.id_co " +
                    "join articol a on a.id_a= d.id_a " +
                    "where extract(year from age(current_date,u.data_nasterii))>25;";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("4.b"))
        {
            //pt a evita dublurile punem d1<d2 si pt avem nevoie de detaliucomanda pt ca nu stim altfel daca s din aaceeasi comanda
            String comanda = "select a1.id_a as id_articol1, a2.id_a as id_articol2 " +
                    "from detaliucomanda d1 " +
                    "join detaliucomanda d2 on d1.id_co = d2.id_co and d1.id_a<d2.id_a " +
                    "join articol a1 on d1.id_a = a1.id_a " +
                    "join articol a2 on d2.id_a=a2.id_a " +
                    "where a1.categorie<>a2.categorie";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("5.a")){
            String comanda = "select c.* " +
                    "from comandaonline c " +
                    "where exists (" +
                    "select 1 " +
                    "from detaliucomanda d " +
                    "join articol a on d.id_a = a.id_a " +
                    "where d.id_co = c.id_co " +
                    "and rating<3);";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("5.b")){
            String comanda = "select c.* " +
                    "from comandaonline c " +
                    "join utilizator u on c.id_u = u.id_u " +
                    "where (current_date-u.data_nasterii)>all( " +
                    "select( current_date-u2.data_nasterii) from utilizator u2 " +
                    "where u2.id_u in( " +
                    "select id_u " +
                    "from comandaonline " +
                    "where data_comanda>='2025-01-01' and data_comanda<'2025-02-01'));";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("6.a")){
            String comanda = "select u.nume,avg(c.suma_totala) as valoare_medie_comanda " +
                    "from utilizator u " +
                    "join comandaonline c on c.id_u = u.id_u " +
                    "where c.data_comanda>='2025-01-01' and c.data_comanda<'2026-01-01' " +
                    "group by u.nume";
            AfisareTabel(model,comanda);
        }
        if(cerinta.equals("6.b")){
            String comanda = "select a.denumire,count(distinct d.id_co) as nr_comenzi,sum(d.cantitate) as cantitate_totala " +
                    "from articol a " +
                    "join detaliucomanda d on d.id_a = a.id_a " +
                    "join comandaonline c on d.id_co = c.id_co " +
                    "where c.data_comanda>='2025-01-01' and c.data_comanda<'2026-01-01' " +
                    "group by a.denumire";
            AfisareTabel(model,comanda);
        }
    }

    private void AfisareTabel(DefaultTableModel model, String sql) {
        model.setRowCount(0);
        model.setColumnCount(0);
        //esential pentru a nu ramane tabelele vechi



        try (Connection conn = PostgresConn.getConnection();
             PreparedStatement statem = conn.prepareStatement(sql);
             ResultSet rs = statem.executeQuery()) {

            //ResultSetMetadata ne da informatii despre tabel,adica coloane si asa
            //e foarte util pentru ca nu mai trebuie sa scriem noi de mana numele fiecarei coloane
            ResultSetMetaData metaData = rs.getMetaData();
            int numarColoane = metaData.getColumnCount();

            //Le adaugam intr un vector(ArrayList dar cerut de swing)
            Vector<String> coloane = new Vector<>();
            for(int i = 1; i <= numarColoane; i++) {
                coloane.add(metaData.getColumnName(i));

            }
            model.setColumnIdentifiers(coloane);

            //obtinem date
            while(rs.next()) {
                Vector<Object> rand = new Vector<>();
                for(int i = 1; i <= numarColoane; i++) {
                    rand.add(rs.getObject(i));

                }
                model.addRow(rand);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Eroare la baza de date: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public JPanel PanelButoane(){
        JPanel panelButoane = new JPanel();
        panelButoane.setBackground(new Color(52, 73, 94));
        panelButoane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        //stilizam butoanele
        btnUtil.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUtil.setBackground(new Color(236, 240, 241));
        btnUtil.setFocusPainted(false);

        btnArticole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnArticole.setBackground(new Color(236, 240, 241));
        btnArticole.setFocusPainted(false);

        btnComenzi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnComenzi.setBackground(new Color(236, 240, 241));
        btnComenzi.setFocusPainted(false);

        panelButoane.add(btnUtil);
        panelButoane.add(btnArticole);
        panelButoane.add(btnComenzi);
        panelButoane.add(btnDetalii);
        panelButoane.add(btnCategorii);

        this.add(panelButoane, BorderLayout.SOUTH);
        return panelButoane;
    }
    private void AfisareTabelFiltre(DefaultTableModel model, String sql) {
        model.setRowCount(0);
        model.setColumnCount(0);
        //esential pentru a nu ramane tabelele vechi

        if (!TabelCurent.equals(UltimTabel)) {
            Coloane.removeAllItems();
        }

        try (Connection conn = PostgresConn.getConnection();
             PreparedStatement statem = conn.prepareStatement(sql);
             ResultSet rs = statem.executeQuery()) {

            //ResultSetMetadata ne da informatii despre tabel,adica coloane si asa
            //e foarte util pentru ca nu mai trebuie sa scriem noi de mana numele fiecarei coloane
            ResultSetMetaData metaData = rs.getMetaData();
            int numarColoane = metaData.getColumnCount();

            //Le adaugam intr un vector(ArrayList dar cerut de swing)
            Vector<String> coloane = new Vector<>();
            for(int i = 1; i <= numarColoane; i++) {
                coloane.add(metaData.getColumnName(i));
                Coloane.addItem(metaData.getColumnName(i));
            }
            model.setColumnIdentifiers(coloane);

            //obtinem date
            while(rs.next()) {
                Vector<Object> rand = new Vector<>();
                for(int i = 1; i <= numarColoane; i++) {
                    rand.add(rs.getObject(i));

                }
                model.addRow(rand);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Eroare la baza de date: " + e.getMessage());
            e.printStackTrace();
        }

        UltimTabel = TabelCurent;//pentru a nu se tot reseta tabelul de Coloane la Sortare
    }


}