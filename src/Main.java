import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class Main {

    private static Connection conec = null;
    private static char separator = ';';

    public static void main(String[] args) throws SQLException, IOException {
        Conexao conexao = new Conexao();
        conec = conexao.abrirConexao();

        File f = new File("arquivos");
        for (File file : f.listFiles()) {
            if (file.isDirectory() || !file.getName().contains(".csv")){
                continue;
            }

            String comandoVerfica = "select table_name from information_schema.tables where table_name = '" + file.getName().replace(".csv", "") + "'";
            List<String> allLines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
            if (allLines.get(0).contains(",")) {
                separator = ',';
            }
            if (allLines.get(0).contains("|")) {
                separator = '|';
            }

            Statement st = conec.createStatement();
            ResultSet rs = st.executeQuery(comandoVerfica);

            while (rs.next()) {
                String delete = "drop table " + rs.getString(rs.getRow());
                conec.prepareStatement(delete).execute();
            }

            System.out.println("Importando: " + file.getName());
//            readDataFromCustomSeperator(file);
        }
    }


    public static void readDataFromCustomSeperator(File file) {
        try {
            FileReader filereader = new FileReader(file);

            CSVParser parser = new CSVParserBuilder().withSeparator(separator).build();
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withCSVParser(parser)
                    .build();
            List<String[]> allData = csvReader.readAll();

            String createTable = "create table " + file.getName().replace(".csv", "") + " (";

            int index = 0;

            for (String campos : allData.get(0)) {

                if (index != 0) {
                    createTable += ", ";
                }

                createTable += campos.replace("\"", "").replace("-", "").replace("?", "") + " TEXT ";

                index++;

            }

            createTable += ")";

            System.out.println(createTable);

            PreparedStatement p = conec.prepareStatement(createTable);
            p.execute();

            for (int i = 1; i < allData.size(); i++) {
                String insert = "insert into " + file.getName().replace(".csv", "") + " ";
                insert += "values (";
                for (int j = 0; j < allData.get(i).length; j++) {
                    if (j != 0) {
                        insert += ",";
                    }
                    insert += "'" + allData.get(i)[j] + "'";
                }
                insert += ")";
                System.out.println(insert);
                conec.prepareStatement(insert).execute();
                System.out.println("inserindo");
            }

            System.out.println("Apos");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
