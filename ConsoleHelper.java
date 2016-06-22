package com.javarush.test.level30.lesson15.big01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by vadym on 27.05.2016.
 */
public class ConsoleHelper
{
    public static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message)
    {
        System.out.println(message);
    }
    public static  String readString()
    {
        String line = null;
        try
        {
            line = bufferedReader.readLine();
        }
        catch (IOException e)
        {
            System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            line = readString();
        }
        return line;
    }
    public static int readInt()
    {
        int intLine = 0;
        try
        {
            intLine = Integer.parseInt(readString());
        }
        catch (NumberFormatException e)
        {
            System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            intLine = readInt();
        }

        return intLine;
    }
}
