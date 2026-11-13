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
import random
def _smooth(pts):
    """closed Catmull-Rom -> cubic path, so the outline is a soft brush edge, not a polygon"""
    n=len(pts); d=f"M{pts[0][0]:.2f} {pts[0][1]:.2f}"
    for i in range(n):
        p0,p1,p2,p3=pts[(i-1)%n],pts[i],pts[(i+1)%n],pts[(i+2)%n]
        c1=(p1[0]+(p2[0]-p0[0])/6,p1[1]+(p2[1]-p0[1])/6); c2=(p2[0]-(p3[0]-p1[0])/6,p2[1]-(p3[1]-p1[1])/6)
        d+=f"C{c1[0]:.2f} {c1[1]:.2f} {c2[0]:.2f} {c2[1]:.2f} {p2[0]:.2f} {p2[1]:.2f}"
    return d+"Z"
def comma(p0,c1,c2,p3,r,tail,n=18,seed=0):
    """One continuous hand-brushed comma: round head, belly swelling a touch, body narrowing into a tail that ends in a soft rounded tip. Slight pressure/hand irregularity."""
    rnd=random.Random(int(p0[0]*10+p0[1]*7+seed))
    def sm(x): x=max(0,min(1,x)); return x*x*(3-2*x)
    ph=rnd.random()*6
    L_,R_=[],[]
    for i in range(n+1):
        t=i/n; x,y=bez(p0,c1,c2,p3,t); dx,dy=dbez(p0,c1,c2,p3,t); m=math.hypot(dx,dy) or 1
        nx,ny=-dy/m,dx/m
        w=((2*r-tail)*(1-sm(t/.40))+tail*(1-t)**.8)/2
        w*=1+0.07*math.sin(ph+t*7)           # pressure drift
        w=max(w,0.55)                          # soft tip, never a needle
        j=0.35*math.sin(ph*2+t*5)              # hand drift off the ideal line
        L_.append((x+nx*(w+j),y+ny*(w+j))); R_.append((x-nx*(w-j),y-ny*(w-j)))
    dx,dy=dbez(p0,c1,c2,p3,0); th=math.atan2(dy,dx)
    cap=[(p0[0]-r*0.92*math.cos(th)*math.sin(k*math.pi/8)*-1+0,p0[1]) for k in range(0)]
    w0=math.hypot(L_[0][0]-R_[0][0],L_[0][1]-R_[0][1])/2
    head=[(p0[0]+w0*math.cos(th-math.pi/2-k*math.pi/6),p0[1]+w0*math.sin(th-math.pi/2-k*math.pi/6)) for k in range(1,6)]
    bx,by=bez(p0,c1,c2,p3,1); ax,ay=bez(p0,c1,c2,p3,.96)
    tip=[(bx+(bx-ax)*0.6,by+(by-ay)*0.6)]
    pts=L_+tip+R_[::-1]+head
    return _smooth(pts)
def one(spec,mirror):
    p0,c1,c2,p3,r,tail=spec
    P=[(mir(p) if mirror else p) for p in (p0,c1,c2,p3)]
    return comma(*P,r,tail)
# Each eye is ONE comma stroke (round head + tail), after the hand sketches (D-31). left eye given, right = mirror unless given.
EYES={
 "stern":   dict(L=((98,67.5),(97,62),(90,59.2),(79,61),4.0,3.2), R=((122,67.5),(122,60),(128,56.6),(137,62),3.9,2.8), mouth="drop"),
 "wry":     dict(L=((98,67),(95,65),(88,62.5),(80,58.4),3.1,2.4), R=None, mouth="dash"),
 "firm":    dict(L=((98,67),(95.5,64.5),(88.5,61.6),(80.5,56.6),3.0,2.4), R=None, mouth="blob"),
 "tender":  dict(L=((99,67.4),(97,60),(90,56.4),(85.4,64.6),3.7,2.4), R=None, mouth="smile"),
 "serene":  dict(L=((99,67.6),(97,60.6),(90.5,57.4),(86,64.8),3.6,2.2), R=None, mouth="soft"),
 "watchful":dict(L=((99,67.6),(97,60.4),(91,57.2),(86.6,64),3.5,2.2), R=None, mouth="line"),
 "worried": dict(L=((99.5,68),(98.4,61.4),(94,56.6),(89,60.6),3.3,2.2), R=None, mouth="oh"),
}
MOUTHS={"smile":"M104 83.5C108 87.4 112 87.4 116 83.5C112 85.6 108 85.6 104 83.5Z","soft":"M105 84.2C108.5 86.6 111.5 86.6 115 84.2C111.5 85.6 108.5 85.6 105 84.2Z",
        "dash":"M105.5 85.5H114.5L114.5 86.4H105.5Z","line":"M105 85.5C108 85 112 85 115 85.5C112 86.1 108 86.1 105 85.5Z",
        "blob":ell(110,85.6,2.6,2.2),"drop":"M106.6 82.6C108.4 81 111.6 81 113.4 82.6C112.6 84.6 111.4 85.8 110 86.6C108.6 85.8 107.4 84.6 106.6 82.6Z",
        "oh":ell(110,86,2.7,3.8)}
def face_parts(kind):
    E=EYES[kind]; parts=[one(E["L"],False), one(E["R"] if E["R"] else E["L"], E["R"] is None)]
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
