/**
 * WIP.
 *
 * The whole idea is that
 * given a path (composed of lines\*), we can slice it into segments then reconnect (some of) them.
 *
 * \* something like [`FlatteningPathIterator`](https://docs.oracle.com/javase/8/docs/api/java/awt/geom/FlatteningPathIterator.html).
 *
 * - A point is an `[x, y]` tuple (`[number, number]`).
 * - A line segment is a tuple of two points.
 *
 * @example
 * SlicedPath.segmentsFromPoints(SlicedPath.cuttingPoints([-10, 20], [20, 75], 4));
 * [[[-10,20],[-2.5,33.75]],  [[-2.5,33.75],[5,47.5]],  [[5,47.5],[12.5,61.25]],  [[12.5,61.25],[20,75]]]
 */
class SlicedPath {

    // ...

    /**
     * Given an array of points (see `cuttingPoints`), `[a, b, c]`,
     * return an array representing line segments: `[ [a,b], [b,c] ]`.
     *
     * @example
     * SlicedPath.segmentsFromPoints([[-10,20], [20,75]]);
     * [ [[-10,20], [20,75]] ]
     * 
     * @example
     * SlicedPath.segmentsFromPoints([[-10,20], [-2.5,33.75], [5,47.5], [12.5,61.25], [20,75]]);
     * [ [[-10,20],[-2.5,33.75]], [[-2.5,33.75],[5,47.5]], [[5,47.5],[12.5,61.25]], [[12.5,61.25],[20,75]]]
     * 
     * @param points {Array<[number, number]>}
     */
    static /*function*/ segmentsFromPoints(points) {
        const lineSegments = [];
        for (let i = 0; i < points.length - 1; i++) {
            const lineSegment = [points[i], points[i + 1]];
            lineSegments.push(lineSegment);
        }
        
        return lineSegments;
    }

    /**
     * Split a line segment (defined by `start` and `end` points) into `nSegments` equal parts.
     * Return an array of points, representing the start of each segment.
     * The `end` point is appended to the array.
     *
     * @example
     * SlicedPath.cuttingPoints([-10,20], [20,75], 1);
     * [[-10,20], [20,75]]
     *
     * @example
     * SlicedPath.cuttingPoints([-10,20], [20,75], 4);
     * [[-10,20], [-2.5,33.75], [5,47.5], [12.5,61.25], [20,75]]
     *
     * @param start {[number, number]}
     * @param end {[number, number]}
     * @param nSegments {number}
     *
     * @returns {Array<[number, number]>}
     */
    static /*function*/ cuttingPoints(start, end, nSegments) {
        const x_delta = (end[0] - start[0]) / nSegments;
        const y_delta = (end[1] - start[1]) / nSegments;
        const points = [];
        for (let i = 1; i < nSegments; i++) {
            const x = start[0] + i * x_delta;
            const y = start[1] + i * y_delta;
            const cutPoint = [x, y];
            points.push(cutPoint);
        }
        const result = [start, ...points, end];
        return result;
    }

}
