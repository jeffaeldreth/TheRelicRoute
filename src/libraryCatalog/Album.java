package libraryCatalog;

public class Album {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private String recordLabel;
    private int onHand;
    private boolean vinyl;
    private boolean cd;
    private boolean cassette;
    private boolean eightTrack;
    private String location;
    private String currentCondition;
    private double selectionValue;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getRecordLabel() { return recordLabel; }
    public void setRecordLabel(String recordLabel) { this.recordLabel = recordLabel; }

    public int getOnHand() { return onHand; }
    public void setOnHand(int onHand) { this.onHand = onHand; }

    public boolean isVinyl() { return vinyl; }
    public void setVinyl(boolean vinyl) { this.vinyl = vinyl; }

    public boolean isCd() { return cd; }
    public void setCd(boolean cd) { this.cd = cd; }

    public boolean isCassette() { return cassette; }
    public void setCassette(boolean cassette) { this.cassette = cassette; }

    public boolean isEightTrack() { return eightTrack; }
    public void setEightTrack(boolean eightTrack) { this.eightTrack = eightTrack; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }

    public double getSelectionValue() { return selectionValue; }
    public void setSelectionValue(double selectionValue) { this.selectionValue = selectionValue; }
}
