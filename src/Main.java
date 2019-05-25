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
import java.util.Arrays;
import java.util.List;

public class Main {

    private static Connection conec = null;
    private static char separator = ';';
    private static Integer totalImport = 3000;

    public static void main(String[] args) throws SQLException, IOException {
        Conexao conexao = new Conexao();
        conec = conexao.abrirConexao();

        File f = new File("arquivos");
        for (File file : f.listFiles()) {
            if (file.isDirectory() || !file.getName().contains(".csv")) {
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
            readDataFromCustomSeperator(file);
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

            int totalCont = Math.round(allData.size() / totalImport);
            boolean insereUm = false;

            int contMaximo = totalImport;
            if (totalCont <= 0) {
                insereUm = true;
                contMaximo = allData.get(0).length;
            }

            int cont = 0;
            int contMin = 0;
            while ((cont <= totalCont) || insereUm) {
                long tempoInicio = System.currentTimeMillis();
                List<String[]> sublista = null;
                if (!insereUm) {
                    if (cont == totalCont) {
                        sublista = allData.subList(contMin, allData.size());
                    } else {
                        sublista = allData.subList(contMin, contMaximo);
                    }
                } else {
                    sublista = allData;
                    insereUm = false;
                    cont = totalCont;
                }

                String insert = "insert into " + file.getName().replace(".csv", "") + " ";
                insert += "values";

                for (String[] colunas : sublista) {
                    if (sublista.indexOf(colunas) == 0){
                        continue;
                    }
                    insert += " (";
                    for (String campo : colunas) {
                        if (Arrays.asList(colunas).indexOf(campo) != 0) {
                            insert += ",";
                        }
                        insert += "'" + campo + "'";
                    }
                    insert += ")";
                    if (sublista.indexOf(colunas) != (sublista.size() - 1)) {
                        insert += ",";
                    }
                }

                conec.prepareStatement(insert).execute();
                System.out.println("insert:" + insert);
                System.out.println("Tempo Total: " + (System.currentTimeMillis() - tempoInicio) / 1000);

                cont++;
                contMin += totalImport;
                contMaximo += totalImport;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
