package uniaoevolutions.view;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Validação e conversão de formato dos campos das telas (CPF/CNPJ, data, valor,
 * telefone, placa, ano, chassi). Apoio da interface, fora da modelagem; as regras
 * de negócio ficam nas controladoras. validarX devolve null ou a mensagem de erro.
 */
public final class Validacoes {

    public static final String MSG_OBRIGATORIO = "Campo obrigatório.";
    public static final String MSG_CPF_INVALIDO = "CPF inválido: confira os 11 dígitos.";
    public static final String MSG_CNPJ_INVALIDO = "CNPJ inválido: confira os 14 dígitos.";
    public static final String MSG_BUSCA_INVALIDA =
            "Informe o nome, o CPF (11 dígitos), o CNPJ (14 dígitos) ou o telefone.";
    public static final String MSG_TELEFONE_INVALIDO = "Telefone inválido. Use o formato (DD) 99999-9999.";
    public static final String MSG_PLACA_INVALIDA = "Placa inválida. Use o formato AAA-9999 ou AAA-9A99.";
    public static final String MSG_CHASSI_INVALIDO =
            "Nº do chassi inválido: 17 letras e números, sem I, O e Q.";
    public static final String MSG_QUANTIDADE_INVALIDA = "Informe uma quantidade de cópias maior que zero.";
    public static final int TAMANHO_MAXIMO_TEXTO = 255;

    private static final DecimalFormat MOEDA = criarFormatoMoeda();

    private Validacoes() {
    }

    // =================================================================
    // CPF e CNPJ
    // =================================================================

    /** CPF: 11 dígitos, não todos iguais, verificadores corretos. */
    public static boolean cpfValido(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 11 || todosIguais(d)) {
            return false;
        }
        for (int n = 9; n <= 10; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += (d.charAt(i) - '0') * (n + 1 - i);
            }
            int dv = (soma * 10) % 11 % 10;
            if (dv != d.charAt(n) - '0') {
                return false;
            }
        }
        return true;
    }

    /** CNPJ: 14 dígitos, não todos iguais, verificadores corretos. */
    public static boolean cnpjValido(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 14 || todosIguais(d)) {
            return false;
        }
        int[][] pesos = {{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}, {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}};
        for (int k = 0; k < 2; k++) {
            int soma = 0;
            for (int i = 0; i < pesos[k].length; i++) {
                soma += (d.charAt(i) - '0') * pesos[k][i];
            }
            int resto = soma % 11;
            int dv = resto < 2 ? 0 : 11 - resto;
            if (dv != d.charAt(12 + k) - '0') {
                return false;
            }
        }
        return true;
    }

    /** "02457731003" -> "024.577.310-03"; "11222333000181" -> "11.222.333/0001-81". */
    public static String formatarCpfCnpj(String digitos) {
        if (digitos == null) {
            return "";
        }
        String d = somenteDigitos(digitos);
        if (d.length() == 11) {
            return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
        }
        if (d.length() == 14) {
            return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/"
                 + d.substring(8, 12) + "-" + d.substring(12);
        }
        return digitos;
    }

    // =================================================================
    // Campo "Cliente": nome, CPF/CNPJ ou telefone
    // =================================================================

    /** Busca do cliente: dígitos = CPF/CNPJ; parênteses, espaço ou "+" = telefone; letras = nome. */
    public static String validarBuscaCliente(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) {
            return MSG_OBRIGATORIO;
        }
        if (t.matches("[0-9./\\-]+")) {
            String d = somenteDigitos(t);
            if (d.length() == 11) {
                // 11 dígitos: CPF ou, se o CPF não fecha, celular.
                if (cpfValido(d) || (t.matches("\\d{11}") && validarTelefone(t) == null)) {
                    return null;
                }
                return MSG_CPF_INVALIDO;
            }
            if (d.length() == 14) {
                return cnpjValido(d) ? null : MSG_CNPJ_INVALIDO;
            }
            if (d.length() == 10) {
                return validarTelefone(t);   // fixo sem pontuação
            }
            return MSG_BUSCA_INVALIDA;
        }
        if (t.matches("[0-9()\\s\\-+]+")) {
            return validarTelefone(t);
        }
        return t.length() < 2 ? MSG_BUSCA_INVALIDA : null;
    }

    // =================================================================
    // Telefone
    // =================================================================

    /** DDD + 8 dígitos (fixo) ou 9 começando por 9 (celular); com ou sem pontuação. */
    public static String validarTelefone(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) {
            return MSG_OBRIGATORIO;
        }
        if (!t.matches("[0-9()\\s\\-]+")) {
            return MSG_TELEFONE_INVALIDO;
        }
        String d = somenteDigitos(t);
        if (d.length() != 10 && d.length() != 11) {
            return MSG_TELEFONE_INVALIDO;
        }
        if (d.charAt(0) == '0' || d.charAt(1) == '0') {
            return MSG_TELEFONE_INVALIDO;
        }
        if (d.length() == 11 && d.charAt(2) != '9') {
            return MSG_TELEFONE_INVALIDO;
        }
        return null;
    }

    /** "67991804402" -> "(67) 99180-4402"; "6733217788" -> "(67) 3321-7788". */
    public static String formatarTelefone(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() == 11) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        }
        if (d.length() == 10) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        }
        return texto == null ? "" : texto.trim();
    }

    // =================================================================
    // Data no formato brasileiro
    // =================================================================

    /** dd/mm/aaaa (ou ddmmaaaa) de uma data que existe; null se inválida. */
    public static Date converterData(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.matches("\\d{8}")) {   // ACRÉSCIMO (rodada 6): data sem barras
            t = t.substring(0, 2) + "/" + t.substring(2, 4) + "/" + t.substring(4);
        }
        if (!t.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            return null;
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false);
        try {
            Date data = formato.parse(t);
            Calendar c = Calendar.getInstance();
            c.setTime(data);
            int ano = c.get(Calendar.YEAR);
            return ano < 1900 || ano > 2999 ? null : data;
        } catch (ParseException erro) {
            return null;
        }
    }

    // =================================================================
    // Valor monetário no formato brasileiro
    // =================================================================

    /** "150", "150,5", "1.234,56" ou "R$ 1.234,56"; null se inválido ou negativo. */
    public static Double converterValor(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.startsWith("R$")) {
            t = t.substring(2).trim();
        }
        if (!t.matches("\\d{1,3}(\\.\\d{3})+(,\\d{1,2})?") && !t.matches("\\d+(,\\d{1,2})?")) {
            return null;
        }
        return Double.parseDouble(t.replace(".", "").replace(",", "."));
    }

    public static String formatarMoeda(double valor) {
        return MOEDA.format(valor);
    }

    /** Valor para o campo de edição: "380,00". */
    public static String formatarValorEditavel(double valor) {
        return MOEDA.format(valor).substring(3);
    }

    // =================================================================
    // Veículo
    // =================================================================

    /** Placa antiga (AAA-9999) ou Mercosul (AAA-9A99), com ou sem hífen. */
    public static String validarPlaca(String texto) {
        String t = texto == null ? "" : texto.trim().toUpperCase();
        if (t.isEmpty()) {
            return MSG_OBRIGATORIO;
        }
        return t.matches("[A-Z]{3}-?\\d[A-Z0-9]\\d{2}") ? null : MSG_PLACA_INVALIDA;
    }

    /** "qaf7c21" -> "QAF-7C21" (placa já validada). */
    public static String normalizarPlaca(String texto) {
        String t = texto.trim().toUpperCase().replace("-", "");
        return t.substring(0, 3) + "-" + t.substring(3);
    }

    /** Ano-modelo: quatro dígitos, de 1950 até o ano que vem. */
    public static String validarAnoModelo(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) {
            return MSG_OBRIGATORIO;
        }
        int limite = Calendar.getInstance().get(Calendar.YEAR) + 1;
        String mensagem = "Ano-modelo inválido: informe um ano entre 1950 e " + limite + ".";
        if (!t.matches("\\d{4}")) {
            return mensagem;
        }
        int ano = Integer.parseInt(t);
        return ano < 1950 || ano > limite ? mensagem : null;
    }

    /** VIN: 17 caracteres, letras e números, sem I, O e Q. */
    public static String validarChassi(String texto) {
        String t = texto == null ? "" : texto.trim().toUpperCase();
        if (t.isEmpty()) {
            return MSG_OBRIGATORIO;
        }
        return t.matches("[A-HJ-NPR-Z0-9]{17}") ? null : MSG_CHASSI_INVALIDO;
    }

    // =================================================================
    // Genéricos
    // =================================================================

    /** Obrigatório; até 255 caracteres. */
    public static String validarObrigatorio(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return MSG_OBRIGATORIO;
        }
        return validarTamanho(texto);
    }

    /** Opcional; até 255 caracteres. */
    public static String validarTamanho(String texto) {
        return texto != null && texto.trim().length() > TAMANHO_MAXIMO_TEXTO
                ? "Máximo de " + TAMANHO_MAXIMO_TEXTO + " caracteres."
                : null;
    }

    /** Inteiro > 0; null se inválido. */
    public static Integer converterQuantidade(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (!t.matches("\\d{1,3}")) {
            return null;
        }
        int quantidade = Integer.parseInt(t);
        return quantidade > 0 ? quantidade : null;
    }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private static boolean todosIguais(String d) {
        return d.chars().distinct().count() == 1;
    }

    private static DecimalFormat criarFormatoMoeda() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("'R$ '#,##0.00", simbolos);
    }
}
