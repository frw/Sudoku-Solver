import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Frederick
 * Date: 7/1/12
 * Time: 12:24 AM
 */
public class DancingLinks {

    private final Node root;
    private final Node[] headers;

    public DancingLinks(boolean[][] matrix, int cols, int rows) {
        root = new Node();
        headers = new Node[cols];
        Node prev = root;
        for (int c = 0; c < cols; c++) {
            Node header = new Node();
            headers[c] = header;
            header.left = prev;
            prev.right = header;
            prev = header;
        }
        root.left = prev;
        prev.right = root;
        Node[] lastNodes = Arrays.copyOf(headers, cols);
        for (int r = 0; r < rows; r++) {
            Node f = null, p = null;
            for (int c = 0; c < cols; c++) {
                if (matrix[c][r]) {
                    Node node = new Node();
                    node.row = r;
                    node.header = headers[c];
                    if (f == null) {
                        f = p = node;
                    } else {
                        node.left = p;
                        p.right = node;
                        p = node;
                    }
                    Node last = lastNodes[c];
                    last.down = node;
                    node.up = last;
                    lastNodes[c] = node;
                }
            }
            if (f != null) {
                f.left = p;
                p.right = f;
            }
        }
        for (int c = 0; c < cols; c++) {
            Node h = headers[c];
            Node l = lastNodes[c];
            l.down = h;
            h.up = l;
        }
    }

    public List<Integer> solve(Set<Integer> cols) {
        if (cols != null) {
            for (int i : cols) {
                cover(headers[i]);
            }
        }
        LinkedList<Integer> solution = new LinkedList<Integer>();
        return search(solution) ? solution : null;
    }

    private boolean search(LinkedList<Integer> solution) {
        if (root.left == root && root.right == root) {
            return true;
        }
        boolean found = false;
        Node c = root.right;
        cover(c);
        for (Node r = c.down; r != c && !found; r = r.down) {
            for (Node j = r.right; j != r; j = j.right) {
                cover(j.header);
            }
            solution.addLast(r.row);
            if (search(solution)) {
                found = true;
            } else {
                solution.removeLast();
            }
            for (Node j = r.left; j != r; j = j.left) {
                uncover(j.header);
            }
        }
        uncover(c);
        return found;
    }

    private void cover(Node header) {
        header.right.left = header.left;
        header.left.right = header.right;
        for (Node i = header.down; i != header; i = i.down) {
            for (Node j = i.right; j != i; j = j.right) {
                j.down.up = j.up;
                j.up.down = j.down;
            }
        }
    }

    private void uncover(Node header) {
        for (Node i = header.up; i != header; i = i.up) {
            for (Node j = i.left; j != i; j = j.left) {
                j.down.up = j;
                j.up.down = j;
            }
        }
        header.right.left = header;
        header.left.right = header;
    }

    private class Node {

        int row;
        Node header, up, down, left, right;
    }

}
