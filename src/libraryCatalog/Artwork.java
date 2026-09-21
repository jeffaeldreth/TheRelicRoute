package libraryCatalog;

public class Artwork {
    private int id;
    private String title;
    private String artist;
    private String medium;
    private String genre;
    private int yearMade;
    private String dimensions;
    private boolean framed;
    private boolean signed;
    private int onHand;
    private String purchasedFrom;
    private String location;
    private String currentCondition;
    private double selectionValue;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getYearMade() { return yearMade; }
    public void setYearMade(int yearMade) { this.yearMade = yearMade; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public boolean isFramed() { return framed; }
    public void setFramed(boolean framed) { this.framed = framed; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public int getOnHand() { return onHand; }
    public void setOnHand(int onHand) { this.onHand = onHand; }

    public String getPurchasedFrom() { return purchasedFrom; }
    public void setPurchasedFrom(String purchasedFrom) { this.purchasedFrom = purchasedFrom; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }

    public double getSelectionValue() { return selectionValue; }
    public void setSelectionValue(double selectionValue) { this.selectionValue = selectionValue; }
}
