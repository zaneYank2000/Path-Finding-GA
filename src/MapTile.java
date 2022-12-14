public class MapTile
{
    //Instance variables
    private MapTile parent;
    private int x; //X coordinate
    private int y; //Y coordinate
    private String type;
    private int retraceCount = 0;

    //constructor
    public MapTile(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public MapTile(int x, int y, String type, MapTile parent) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.parent = parent;
    }

    //parent
    public MapTile getParent() {
        return parent;
    }
    public void setParent(MapTile parent) {
        this.parent = parent;
    }

    //x
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    //y
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    //retrace count
    public int getRetraceCount() {
        return retraceCount;
    }
    public void setRetraceCount(int retraceCount) {
        this.retraceCount = retraceCount;
    }

    //type
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
