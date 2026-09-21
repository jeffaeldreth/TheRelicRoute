package libraryCatalog;

public class Movie {
    private int id;
    private String title;
    private String director;
    private String leadingActors;
    private String genre;
    private String rating;
    private int runTime;
    private int onHand;
    private boolean dvd;
    private boolean bluray;
    private boolean uhd4k;
    private boolean vhs;
    private String location;
    private String currentCondition;
    private double selectionValue;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getLeadingActors() { return leadingActors; }
    public void setLeadingActors(String leadingActors) { this.leadingActors = leadingActors; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public int getRunTime() { return runTime; }
    public void setRunTime(int runTime) { this.runTime = runTime; }

    public int getOnHand() { return onHand; }
    public void setOnHand(int onHand) { this.onHand = onHand; }

    public boolean isDvd() { return dvd; }
    public void setDvd(boolean dvd) { this.dvd = dvd; }

    public boolean isBluray() { return bluray; }
    public void setBluray(boolean bluray) { this.bluray = bluray; }

    public boolean isUhd4k() { return uhd4k; }
    public void setUhd4k(boolean uhd4k) { this.uhd4k = uhd4k; }

    public boolean isVhs() { return vhs; }
    public void setVhs(boolean vhs) { this.vhs = vhs; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }

    public double getSelectionValue() { return selectionValue; }
    public void setSelectionValue(double selectionValue) { this.selectionValue = selectionValue; }
}
