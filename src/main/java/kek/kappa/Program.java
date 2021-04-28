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
        Student stud = new Student(idnp);
        WriteToFile(stud.getJSON().toString(), "date.json");
    }

    static void WriteToFile(String data, String filename)
    {
        try {
            PrintWriter out = new PrintWriter(filename);
            out.println(data);
            out.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }
}
