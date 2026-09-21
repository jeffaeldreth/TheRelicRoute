package libraryCatalog;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private int onHand;
    private boolean hardback;
    private boolean paperback;
    private String location;
    private String currentCondition;
    private int numberOfPages;
    private double selectionValue;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getOnHand() { return onHand; }
    public void setOnHand(int onHand) { this.onHand = onHand; }

    public boolean isHardback() { return hardback; }
    public void setHardback(boolean hardback) { this.hardback = hardback; }

    public boolean isPaperback() { return paperback; }
    public void setPaperback(boolean paperback) { this.paperback = paperback; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }

    public int getNumberOfPages() { return numberOfPages; }
    public void setNumberOfPages(int numberOfPages) { this.numberOfPages = numberOfPages; }

    public double getSelectionValue() { return selectionValue; }
    public void setSelectionValue(double selectionValue) { this.selectionValue = selectionValue; }
}
