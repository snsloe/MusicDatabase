public class MusicRecord {
    private int id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private int duration;

    public MusicRecord(int id, String title, String artist, String album, String genre, int duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
    }

    public int getId() { 
        return id; 
    }
    public String getTitle() { 
        return title; 
    }
    public String getArtist() { 
        return artist; 
    }
    public String getAlbum() { 
        return album; 
    }
    public String getGenre() { 
        return genre; 
    }
    public int getDuration() { 
        return duration; 
    }

    public void setId(int id) { 
        this.id = id; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }
    public void setArtist(String artist) { 
        this.artist = artist; 
    }
    public void setAlbum(String album) { 
        this.album = album; 
    }
    public void setGenre(String genre) { 
        this.genre = genre; 
    }
    public void setDuration(int duration) { 
        this.duration = duration; 
    }
}
