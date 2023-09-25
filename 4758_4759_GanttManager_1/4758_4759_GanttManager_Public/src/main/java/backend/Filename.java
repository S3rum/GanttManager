package backend;

public class Filename {
    private String fullPath;

    public Filename(String str) {
        fullPath = str;
    }
    public String getFilename() {
        int dot = fullPath.lastIndexOf(".");
        int sep = fullPath.lastIndexOf("\\");
        return fullPath.substring(sep + 1, dot);
    }
    public static void main(String[] args) {
        Filename filename = new Filename("C:\\Users\\srig\\Desktop\\Uni\\Java program\\java\\2022-23_AM1_AM2_AM3_GanttManager_Public\\src\\main\\resources\\input\\Eggs.tsv");
        System.out.println(filename.getFilename());
    }
}