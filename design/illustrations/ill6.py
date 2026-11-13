import math,json,re
INK="#12302F"; TEAL="#23706B"
def circ(cx,cy,r): return f"M{cx-r} {cy}A{r} {r} 0 1 1 {cx+r} {cy}A{r} {r} 0 1 1 {cx-r} {cy}Z"
def heart(cx,cy,k=1.0):
    p=lambda x,y:f"{cx+x*k:.1f} {cy+y*k:.1f}"
    return f"M{p(0,16)}C{p(-26,0)} {p(-20,-20)} {p(-6,-19)}C{p(-2,-19)} {p(0,-16)} {p(0,-16)}C{p(0,-16)} {p(2,-19)} {p(6,-19)}C{p(20,-20)} {p(26,0)} {p(0,16)}Z"
def rot(d,deg,cx,cy):
    a=math.radians(deg); c,s=math.cos(a),math.sin(a)
    def f(m):
        x,y=float(m.group(1)),float(m.group(2)); return f"{cx+(x-cx)*c-(y-cy)*s:.1f} {cy+(x-cx)*s+(y-cy)*c:.1f}"
    return re.sub(r'(-?\d+\.?\d*) (-?\d+\.?\d*)',f,d)
# --- the mum: slim egg head with a bun, long single-sweep body that tapers into a curled tail (no legs)
def ell(cx,cy,rx,ry):
    k=.5523; return (f"M{cx-rx} {cy}C{cx-rx} {cy-ry*k:.2f} {cx-rx*k:.2f} {cy-ry} {cx} {cy-ry}C{cx+rx*k:.2f} {cy-ry} {cx+rx} {cy-ry*k:.2f} {cx+rx} {cy}"
                     f"C{cx+rx} {cy+ry*k:.2f} {cx+rx*k:.2f} {cy+ry} {cx} {cy+ry}C{cx-rx*k:.2f} {cy+ry} {cx-rx} {cy+ry*k:.2f} {cx-rx} {cy}Z")
HEAD="M110 18C134 18 148 37 148 60C148 84 132 102 110 102C88 102 72 84 72 60C72 37 86 18 110 18Z"
HAIR="M73 56C75 34 91 22 110 22C130 22 146 36 148 54C136 44 122 44 110 50C98 58 86 56 73 56Z"
BUN="M92 24C88 8 100 1 112 3C124 5 128 17 123 25Z"
EARL="M67 52C63 60 63 68 67 76"; EARR="M153 52C157 60 157 68 153 76"
BODY="M99 102C90 114 82 130 80 152C77 180 92 205 122 213C146 219 166 209 166 193C166 182 156 177 150 183C146 187 148 193 154 195"
BODY2="M121 102C128 114 134 128 138 144C144 162 152 174 162 178"
def L(x): return 220-x   # mirror about the head's centre line
def pth(pts): return pts
def solid_eye(h, mirror, droop=0):
    """Eye and brow in one stroke: a round pupil near the nose whose comma tail arcs up and out over it like an eyebrow."""
    X=(lambda x: L(x)) if mirror else (lambda x: x)
    r=2.3+h*.24; cx=101.0; cy=66.5; lift=60.2-h*.25
    return (f"M{X(83.5)} 63.6C{X(89)} {lift-1.2:.1f} {X(96)} {lift-1.4:.1f} {X(cx-r*.6)} {cy-r*.95:.1f}"
            f"C{X(cx+r*.2)} {cy-r*1.05:.1f} {X(cx+r)} {cy-r*.6:.1f} {X(cx+r)} {cy:.1f}"
            f"C{X(cx+r)} {cy+r*.7:.1f} {X(cx+r*.55)} {cy+r:.1f} {X(cx)} {cy+r:.1f}"
            f"C{X(cx-r*.75)} {cy+r:.1f} {X(cx-r*1.05)} {cy+r*.3:.1f} {X(cx-r*1.05)} {cy-r*.1:.1f}"
            f"C{X(97)} {lift+2.6:.1f} {X(90)} {lift+2.0:.1f} {X(83.5)} 63.6Z")
def solid_brow(yo,yi,mirror,t=2.4,arch=4):
    X=(lambda x: L(x)) if mirror else (lambda x: x)
    return (f"M{X(85)} {yo}C{X(91)} {yo-arch} {X(98)} {yi-arch} {X(104)} {yi}"
            f"L{X(104)} {yi+t}C{X(98)} {yi-arch+t*.6:.1f} {X(91)} {yo-arch+t*.5:.1f} {X(85)} {yo}Z")
MOUTHS={"smile":"M103 84C107 88 114 88 118 83C114 86.4 107 86.4 103 84Z","smirk":"M103 85C107 88.4 114 88 120 82C114 85.6 107 86.2 103 85Z",
        "soft":"M104 85C108 87.6 113 87.6 117 84.6C113 86.4 108 86.4 104 85Z","flat":"M104 86.5C109 85.6 113 85.8 117 84.4C113 86.8 109 87.4 104 86.5Z",
        "line":"M105 86C109 85.4 112 85.4 115 86C112 86.6 109 86.6 105 86Z"}
TILT={"stern":11,"worried":-9,"wry":4,"tender":-2}
def face_parts(op,dx,dy,b,mouth,r=3.4,tilt=0):
    """No eyebrows (D-30): expression = eye openness, eye tilt, mouth."""
    h=2+op*7
    parts=[rot(solid_eye(h,False),tilt,96,65),rot(solid_eye(h,True),-tilt,124,65)]
    parts.append(ell(110,86,2.6,3.6) if mouth=="oh" else MOUTHS[mouth])
    return [],parts
FACES={ # open, look dx, look dy, brows (outerL,innerL,outerR,innerR), mouth
 "serene": (.55,0,1.2,(54,55,54,55),"soft"),
 "tender": (.8,-1.2,1,(54,56,54,56),"smirk"),
 "wry":    (.7,1.2,0,(52,52,56,57),"smirk"),
 "stern":  (.45,0,0,(50,57,50,57),"flat"),
 "worried":(.85,0,.5,(57,51,57,51),"oh"),
 "watchful":(1,0,-1.5,(55,54,55,54),"line"),
}
def eyes(kind):
    if kind in FACES:
        op,dx,dy,b,m=FACES[kind]; return face_parts(op,dx,dy,b,m,tilt=TILT.get(kind,0))
    if kind=="happy":
        return ["M86 68C89 57 101 57 104 68","M116 68C119 57 131 57 134 68","M86 49C91 45 100 45 104 49","M134 49C129 45 120 45 116 49","M99 83C107 92 113 92 121 83"],[]
    if kind=="sleep":
        return ["M86 62C89 72 101 72 104 62","M116 62C119 72 131 72 134 62","M103 86C108 89 112 89 117 86"],[]
CLASP=["M121 110C146 118 150 146 126 152","M99 110C80 120 86 146 112 152","M112 152C118 156 124 156 128 152"]
def arms(): return ["M121 110C147 116 153 144 133 160C123 168 108 166 97 158"]
def robot(kind):
    e,d=eyes(kind)
    head=[rot(x,-5,110,102) for x in [HEAD,EARL,EARR]+e]
    hairs=[rot(x,-5,110,102) for x in [HAIR,BUN]]
    return head+[BODY,BODY2], d, hairs
S={}
def scene(n,col,kind,prop_line,prop_fill,tx=6,ty=6,sc=1.0,front=None,extra=(),hands=False):
    rb,d,br=robot(kind)
    dots=[rot(x,-5,110,102) for x in d]
    S[n]=dict(col=col,robot=rb,dots=dots,brand=br,prop=prop_line,fill=prop_fill,arms=(CLASP if hands else arms()),extra=list(extra),tx=tx,ty=ty,sc=sc)
H=heart(114,136,.85)
scene("nothing_owed","#B3265B","tender",[H],[H])
CK=circ(114,134,19)
scene("all_kept","#23706B","tender",[CK,"M105 134L112 141L124 125"],[CK],extra=["M174 40V52M168 46H180","M30 70V80M25 75H35","M180 110V118M176 114H184"])
SUN="M152 172A38 38 0 0 1 228 172Z"
scene("clear_sky","#F5A524","serene",["M142 172H238","M190 120V132","M222 130L215 138","M158 130L165 138"],[SUN],tx=-14,ty=6,hands=True)
PG="M98 116H132V154H98Z"
scene("blank_page","#F5A524","watchful",[PG,"M104 128H126","M104 138H118"],[PG])
MN="M122 116A22 22 0 1 0 132 156A17 17 0 0 1 122 116Z"
scene("quiet_day","#2563B8","sleep",[MN],[MN],extra=["M176 50V60M171 55H181","M30 96V104M26 100H34"])
scene("no_one_yet","#2563B8","watchful",[],[],extra=[circ(182,90,10),"M164 134C164 116 173 110 182 110C191 110 200 116 200 134"])
for k in ("stern","worried","wry"): scene("face_"+k,"#B3265B",k,[],[])
json.dump(S,open("scenes6.json","w"))
def svg(names,SW=1.6,cols=5):
    out=""
    for i,n in enumerate(names):
        s=S[n]; x=(i%cols)*250; y=(i//cols)*250
        g=f'<g transform="translate({x+5},{y+5})" fill="none" stroke="{INK}" stroke-width="{SW}" stroke-linecap="round" stroke-linejoin="round">'
        for d in s["fill"]: g+=f'<path d="{d}" fill="{s["col"]}" fill-opacity=".38" stroke="none"/>'
        g+=f'<g transform="translate({s["tx"]},{s["ty"]}) scale({s["sc"]})">'
        for d in s["brand"]: g+=f'<path d="{d}" fill="{TEAL}" fill-opacity=".45" stroke="none"/>'
        g+="".join(f'<path d="{d}"/>' for d in s["robot"])+"".join(f'<path d="{d}" fill="{INK}" stroke="none"/>' for d in s["dots"])
        g+="".join(f'<path d="{d}"/>' for d in s["prop"]+s["arms"]+s["extra"])+'</g>'
        out+=g+"</g>"
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="1250" height="500"><rect width="1250" height="500" fill="#F2F8F7"/>{out}</svg>'
open("sheet6.svg","w").write(svg(list(S)))
