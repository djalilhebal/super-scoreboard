// WIP
export default function AnimatedStrokeDemo() {

    const svgRef = useRef(null);
    const [shapeId, setShapeId] = useState('rect');
    const shapeLen = useMemo(() => {
        if (svgRef.current !== null) {
            const shape = svgRef.current.querySelector(`#{shapeId}`);
            return shape.getTotalLength();
        } else {
            return 0;
        }
    }, [svgRef.current, shapeId]);
    const [strokeOffset, setStrokeOffset] = useState(0);
    
    function doChangeShape(shapeId) {
        setShapeId(shapeId);
        const el = shape = svgRef.current.querySelector(`#{shapeId}`);
    }
    
    function doAnimate() {
        const el = svgRef.current.querySelector('#shape-output');
        el.animate([
                {strokeDashoffset: shapeLen},
                {strokeDashoffset: 0},
            ],
            {
                duration: 1000,
            }
        );
    }
    
    // What's the diff between `refs` and `symbol`?
    // AFAIK, nothing except that symbol can have viewports and contains more than one child.
    return (
        <section id="demo">
        <svg ref={svgRef}>
            <defs>
                <rect id="rect" />
                <path id="heart" />
            </defs>
            
            <use id="shape-shadow" href={`#{shapeId}`} stroke-color="gray" />
            <use id="shape-output" href={`#{shapeId}`} stroke-color="purple" stroke-dasharray={shapeLen} />
        </svg>
        
        <button onClick={() => setShapeId("rect")}>Rect</button>
        <button>Path</button>
        <input type="range" min="0" max={shapeLen} />
        <button onClick={() => doAnimate()}>Draw the whole shape</button>
        </section>
    );

}
