import static java.lang.Math.random;
public class Wall {
    int y1 = (int) (Math.random() * 700);;
    int y2;
    int x1 = 850;
    int vx1 = 1;

    Wall () {
        this.y1 = y1;
        this.y2 = y2;
        this.x1 = x1;
        this.vx1 = vx1;
    }
}