import java.sql.Connection;
import java.sql.DriverManager;


public class Conexao {
    private Connection conexao;
    private String USER = "root";
    private String SENHA = "password";
    private String SERVER = "localhost";
    private String DATABASE_NAME = "testeCSVToJson";
    private String PORTA = "3306";

    public Conexao() {
    }

    public Connection abrirConexao() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conexao = DriverManager.getConnection("jdbc:mysql://" + this.SERVER + ":" + this.PORTA + "/" + this.DATABASE_NAME, this.USER, this.SENHA);
            if (this.conexao != null) {
                System.out.println("Conexao criada");
            } else {
                System.out.println("Sem Conexao");
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return this.conexao;
    }

    public void fecharConexao() {
        try {
            this.conexao.close();
        } catch (Exception var2) {
        }

    }
}
