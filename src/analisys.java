import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


import java.io.*;
import java.net.FileNameMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;


public class analisys {

    public static void main(String[] args) {
        //запрашиваем путь к папке, содержашей файлы для анализа
        Scanner sc = new Scanner(System.in);
        System.out.println("Введи что-ли путь к папке... или давай как обычно?");
        String path = sc.nextLine();

        //проверяем путь на валидность и наличие файлов
        while (true) {
            if (path.contains("давай как обычно")) {
                path = "D:\\firelabs\\21052022\\json";
                break;
            } else if (new File(path).isFile()) {
                System.out.println("Вообще то это просто файл, давай не косячь, дай имя папки!");
                path = sc.nextLine();

            } else if (!new File (path).exists() && path.contains(":\\")){
                System.out.println("Такая папка не существует! Давай еще раз, только теперь сначала подумай...");
                path = sc.nextLine();

            } else if (new File(path).listFiles().length == 0){
                System.out.println("А каталог то пустой! Может хоть будешь проверять путь перед вводом?");
                path = sc.nextLine();

            } else if (path.contains(":\\")) {
                break;
            } else {
                System.out.println("Слышь, криворукий, давай еще разок трайни, ничё не понятно");
                path = sc.nextLine();

            }
        }
        sc.close();
        //получаем перечень наименований файлов из папки
        File [] pathNames;
        File f = new File (path);
        pathNames = f.listFiles();
        ArrayList<String> fullNames = new ArrayList<>();
        System.out.println("количество файлов - " + pathNames.length);
        for (File pathName : pathNames) {
            fullNames.add(pathName.getPath());
            //System.out.println(pathNames.length);
        }
        //получаем набор поставлений/действий/наименований типов документов
        ArrayList<String> namesOfResolutions = listOfNames.getArrayOfNames();

        System.out.println("количество наименований документов в анализе - "+namesOfResolutions.size());
        String [][] fullBase = new String [pathNames.length+1][namesOfResolutions.size()+8];
        //устанавливаем верхнюю строку
        fullBase[0][0] = "идентификатор";
        fullBase[0][1] = "тип ответа";
        fullBase[0][2] = "№ ИП / причина отказа";
        for (int i = 0; i < namesOfResolutions.size(); i++) {
            fullBase[0][i+3]=namesOfResolutions.get(i);
        }
        fullBase[0][3+namesOfResolutions.size()] = "Счета";
        fullBase[0][4+namesOfResolutions.size()] = "Место работы";
        fullBase[0][5+namesOfResolutions.size()] = "Транспорт";
        fullBase[0][6+namesOfResolutions.size()] = "Недвижимость";
        fullBase[0][7+namesOfResolutions.size()] = "Иное";
        //заполняем базу по всем файлам

        for (int i = 0; i < fullNames.size(); i++) {
            //прогоняем файлы через счетчик
            ArrayList<String> counts = countDocs(fullNames.get(i));
            for (int j = 0; j < counts.size(); j++) {
                fullBase[i+1][j] = counts.get(j);
                //System.out.println(counts.get(j));
            }
        }
        //выводим в txt
        txtOutput(fullBase, path);
        //выводим в эксель
        //excelOutput(fullBase, path);

    }
    //метод для анализа каждого переданного файла
    public static ArrayList <String> countDocs (String filename) {
        ArrayList<String> counts = new ArrayList<>();

        //создаем счетчики
        int [] count = new int[listOfNames.getArrayOfNames().size()];
        String deloNumForSeach = "<fssp:DeloNum>";
        String reject = "Уведомление об отсутствии исполнительного производства";
        String notReject = "Уведомление о ходе исполнительного производства";
        String negative = "Уведомление об отказе в предоставлении информации об исполнительном производстве";
        String message = "Сообщение (уведомление) с ответом на запрос";
        String content = "";

        //идентификатор первой строкой
        counts.add(filename.substring(filename.lastIndexOf("\\")+1,filename.lastIndexOf(" ")));
        //работаем с вложенными директориями
        File file = new File(filename);
        if (file.isDirectory()){
            File [] pathNames;
            File f = new File (filename);
            pathNames = f.listFiles();
            ArrayList <String> fullNamesForSubdir = new ArrayList<>();
            for (File pathName : pathNames) {
                fullNamesForSubdir.add(pathName.getPath());
            }
            try{
                for (String s : fullNamesForSubdir) {
                    content += Files.lines(Paths.get(s)).reduce("", String::concat);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Проблемы с чтением файла");
            }
        } else {
            //переводим все содержание файла в строку
            try {
                content = Files.lines(Paths.get(filename)).reduce("", String::concat);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Проблемы с чтением файла");
            }
        }

        //System.out.println(deloNum);
        //определяем тип ответа
        if (content.contains(reject)){
            counts.add(reject);
            content = content.substring(content.indexOf("Text")+5);
            counts.add(content.substring(0,content.indexOf("</")));
            return counts;
        } else if (content.contains(notReject)){
            counts.add(notReject);
        } else if (content.contains(negative)){
            counts.add(negative);
            content = content.substring(content.indexOf("Text")+5);
            counts.add(content.substring(0,content.indexOf("</")));
            return counts;
        } else if (counts.contains(message)) {
            counts.add(message);
            content = content.substring(content.indexOf("Text")+5);
            counts.add(content.substring(0,content.indexOf("</")));
            return counts;
        } else counts.add("фигня какая-то");

        //ищем и вставляем в базу номер ИП
        try {
            String deloNum = content.substring(content.indexOf(deloNumForSeach)+deloNumForSeach.length(),content.indexOf("</fssp:DeloNum>"));
            counts.add(deloNum);}
        catch (Exception e) {
            System.out.println(e + "в файле " + filename);}

        //считаем постановления
        for (int i = 0; i < listOfNames.getArrayOfNames().size(); i++) {
            while (content.contains(listOfNames.getArrayOfNames().get(i))){
                content = content.substring(0,content.indexOf(listOfNames.getArrayOfNames().get(i)))+
                        content.substring(content.indexOf(listOfNames.getArrayOfNames().get(i))+listOfNames.getArrayOfNames().get(i).length());
                count[i]++;
            }
        }

        for (int j : count) {
            counts.add("" + j);
        }
        ArrayList <String> namesOfDescriptions = new ArrayList<>(Arrays.asList(
                "Счет",
                "Место получения дохода",
                "Транспорт",
                "Недвижимость"
        ));
        //заводим описания по счетам, машинам, недвижимости
        String accounts = "";
        String placeOfWork = "";
        String transport = "";
        String estate = "";
        String other = "";
        while (content.contains("<fssp:Description>")){
            content = content.substring(content.indexOf("<fssp:Description>")+"<fssp:Description>".length());
            String s = content.substring(0, content.indexOf("</fssp:Description"));
            if (s.contains("Счет")){
                s = s.substring(s.indexOf("|")+1, s.indexOf("|")+21);
                if (!accounts.contains(s)) accounts += s + " ";
            } else if (s.contains("Место получения дохода")) {
                s = s.substring(s.indexOf("|")+1);
                if (!placeOfWork.contains(s)) placeOfWork += s + " ";
            } else if (s.contains("Транспорт")) {
                s = s.substring(s.indexOf("|")+1);
                if (!transport.contains(s)) transport += s + " ";
            } else if (s.contains("Недвижимость")) {
                s = s.substring(s.indexOf("|")+1);
                if (!estate.contains(s)) estate += s + " ";
            } else {
                if (!other.contains(s)) other += s + " ";
            }

            content = content.substring(content.indexOf("</fssp:Description"));
        }

        //добавляем счета и т.п.
        counts.add(accounts);
        counts.add(placeOfWork);
        counts.add(transport);
        counts.add(estate);
        counts.add(other);
        //возвращаем список
        return counts;
    }
    // метод для вывода в txt
    private static void txtOutput(String[][] fullBase, String path) {
        try (FileWriter writer = new FileWriter(path.substring(0, path.lastIndexOf('\\')+1)+"result.txt", false)){
            for (int i = 0; i < fullBase.length; i++) {
                for (int j = 0; j < fullBase[i].length; j++) {
                    writer.write(fullBase[i][j] + ";");
                }
                writer.write("\n");
            }
            writer.close();
            System.out.println("Все ок, ищи файл " + path.substring(0, path.lastIndexOf('\\')+1)+"result.txt");
        } catch (IOException ex) {

        }

    }

    //метод для вывода в excel
//    public static void excelOutput (String [][] fullBase, String path) {
//        String fileName = path.substring(0, path.lastIndexOf("\\")+1) + "report1.xlsx";
//        System.out.println(fileName);
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet sheet = workbook.createSheet("Анализ ответов приставов");
//
//        int rowNum = 0;
//        System.out.println("Заливаем данные");
//
//        for (String[] string : fullBase) {
//            Row row = sheet.createRow(rowNum++);
//            int colNum = 0;
//            for (String field : string) {
//                Cell cell = row.createCell(colNum++);
//                cell.setCellValue((String) field);
//            }
//            try {
//                FileOutputStream outputStream = new FileOutputStream(fileName);
//                workbook.write(outputStream);
//                workbook.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }






}

