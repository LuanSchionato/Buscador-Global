package geopolitico.util;

import java.util.Scanner;

public final class InputUtils {

    private InputUtils() { /* utilitário, não instanciável */ }

    public static long lerLong(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return 0L;
        try {
            return Long.parseLong(s.replaceAll("[.,\\s]", ""));
        } catch (NumberFormatException e) {
            System.out.println("  Valor inválido — filtro ignorado.");
            return 0L;
        }
    }

    public static double lerDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("  Valor inválido — filtro ignorado.");
            return 0.0;
        }
    }

    public static int[] lerOrdenacao(Scanner scanner, String prompt1, String prompt2) {
        System.out.print(prompt1);
        String c = scanner.nextLine().trim();
        System.out.print(prompt2);
        String o = scanner.nextLine().trim();

        int criterio = switch (c) {
            case "1" -> 1; case "2" -> 2; case "3" -> 3; default -> -1;
        };
        int ordem = switch (o) {
            case "1" -> 1; case "2" -> 2; default -> -1;
        };

        if (criterio == -1 || ordem == -1) {
            System.out.println("  Opção inválida.");
            return null;
        }
        return new int[]{criterio, ordem};
    }
}
