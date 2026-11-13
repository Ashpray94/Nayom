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
def bez(p0,c1,c2,p3,t):
    u=1-t; return (u**3*p0[0]+3*u*u*t*c1[0]+3*u*t*t*c2[0]+t**3*p3[0], u**3*p0[1]+3*u*u*t*c1[1]+3*u*t*t*c2[1]+t**3*p3[1])
def dbez(p0,c1,c2,p3,t):
    u=1-t; return (3*u*u*(c1[0]-p0[0])+6*u*t*(c2[0]-c1[0])+3*t*t*(p3[0]-c2[0]), 3*u*u*(c1[1]-p0[1])+6*u*t*(c2[1]-c1[1])+3*t*t*(p3[1]-c2[1]))
def brush(p0,c1,c2,p3,w0,w1=.15,n=26):
    """A brush flick: a cubic centre-line that tapers from w0 to w1, as a closed filled outline."""
    L_,R_=[],[]
    for i in range(n+1):
        t=i/n; x,y=bez(p0,c1,c2,p3,t); dx,dy=dbez(p0,c1,c2,p3,t); m=math.hypot(dx,dy) or 1
        nx,ny=-dy/m,dx/m; w=(w0*(1-t)**.85+w1*t)/2
        L_.append((x+nx*w,y+ny*w)); R_.append((x-nx*w,y-ny*w))
    pts=L_+R_[::-1]
    return "M"+"L".join(f"{x:.2f} {y:.2f}" for x,y in pts)+"Z"
def mir(pt): return (L(pt[0]),pt[1])
def dot(cx,cy,rx,ry,mirror): return ell(L(cx) if mirror else cx,cy,rx,ry)
def tail(spec,mirror):
    P=[(mir(p) if mirror else p) for p in spec[:4]]
    return brush(P[0],P[1],P[2],P[3],spec[4],spec[5] if len(spec)>5 else .15)
# Each face is a pair of brush-drawn eyes (a dot plus a flick tail) and a tiny mouth, after the hand sketches (D-31).
# eye = (dot cx,cy,rx,ry), tail = (p0,c1,c2,p3,w0[,w1]); left eye drawn, right = mirror unless given.
EYES={
 # 1 lofty / stern: a long flat sweep on one side, a short hook on the other
 "stern":   dict(L=((98,67.5,3.7,4.3),((96,62.4),(91,59.6),(86,59.4),(79,61.2),3.4)), R=((122,67.5,3.7,4.3),((123,62),(126,57.2),(132,57.2),(137,62),2.8)), mouth="drop"),
 # 2 wry: small dots, tails rising outward
 "wry":     dict(L=((98,67,3.1,2.9),((95,64.2),(90,63),(85,61),(80,58.4),2.6)), R=None, mouth="dash"),
 # 3 firm: steeper V
 "firm":    dict(L=((98,67,3.0,2.8),((95.5,65),(90,63.4),(85,60.6),(80.5,56.6),2.6)), R=None, mouth="blob"),
 # 4 playful / kind: dots cupped by a curling hook
 "tender":  dict(L=((99,67.4,3.7,3.4),((96,62.6),(91,57.8),(85.5,60.4),(86.5,66.4),2.2,.5)), R=None, mouth="smile"),
 "serene":  dict(L=((99,67.6,3.6,3.2),((96,62.8),(91,58.6),(86,60.8),(86.5,66.2),2.0,.5)), R=None, mouth="soft"),
 "watchful":dict(L=((99,67.4,3.5,3.8),((96,62.4),(91.5,58.4),(86.5,60.4),(87,65.6),2.0,.5)), R=None, mouth="line"),
 # 5 wide-eyed: tall oval pupils with a small hook above
 "worried": dict(L=((99.5,68,2.9,5.0),((97.5,62),(95.5,57.4),(91,57.6),(89,61.6),2.0,.4)), R=None, mouth="oh"),
}
MOUTHS={"smile":"M104 83.5C108 87.4 112 87.4 116 83.5C112 85.6 108 85.6 104 83.5Z","soft":"M105 84.2C108.5 86.6 111.5 86.6 115 84.2C111.5 85.6 108.5 85.6 105 84.2Z",
        "dash":"M105.5 85.5H114.5L114.5 86.4H105.5Z","line":"M105 85.5C108 85 112 85 115 85.5C112 86.1 108 86.1 105 85.5Z",
        "blob":ell(110,85.6,2.6,2.2),"drop":"M106.6 82.6C108.4 81 111.6 81 113.4 82.6C112.6 84.6 111.4 85.8 110 86.6C108.6 85.8 107.4 84.6 106.6 82.6Z",
        "oh":ell(110,86,2.7,3.8)}
def face_parts(kind):
    E=EYES[kind]; parts=[]
    (dx,dy,rx,ry),tl=E["L"]; parts+= [dot(dx,dy,rx,ry,False),tail(tl,False)]
    R=E["R"]
    if R is None: parts+=[dot(dx,dy,rx,ry,True),tail(tl,True)]
    else:
        (rdx,rdy,rrx,rry),rtl=R; parts+=[ell(rdx,rdy,rrx,rry),brush(rtl[0],rtl[1],rtl[2],rtl[3],rtl[4],.15)]
    parts.append(MOUTHS[E["mouth"]])
    return [],parts
def eyes(kind):
    if kind in EYES: return face_parts(kind)
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
for k in ("stern","worried","wry","firm"): scene("face_"+k,"#B3265B",k,[],[])
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
