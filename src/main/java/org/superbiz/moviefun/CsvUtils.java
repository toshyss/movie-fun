package org.superbiz.moviefun;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvUtils {

    public static String readFile(String path) {
        try {
            //Scanner scanner = new Scanner(new File(path)).useDelimiter("\\A");
            ClassLoader classLoader =  CsvUtils.class.getClassLoader();
            InputStream inputstream = classLoader.getResourceAsStream(path);
            Scanner scanner = new Scanner(inputstream).useDelimiter("\\A");


            if (scanner.hasNext()) {
                //System.out.println("DEBUG!!!!!!!!!!!!!!!" + scanner.next());
                return scanner.next();
            } else {
                return "";
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> readFromCsv(ObjectReader objectReader, String path) {
        try {
            List<T> results = new ArrayList<>();

            MappingIterator<T> iterator = objectReader.readValues(readFile(path));

            while (iterator.hasNext()) {
                results.add(iterator.nextValue());
            }

            return results;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
