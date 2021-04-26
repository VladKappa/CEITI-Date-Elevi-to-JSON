package kek.kappa;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Program {
    public static void main(String[] args)
    {
        Scanner input = new Scanner(System.in);
        System.out.print("IDNP: ");
        String idnp = input.next();
        input.close();
        Elev e = new Elev(idnp);
        try {
            PrintWriter out = new PrintWriter("date.json");
            out.println(e.getJSON());
            out.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }
}
