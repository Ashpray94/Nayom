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
def almond(_):
    # leaf-shaped eyes, outer corners a touch lower, as in the reference
    return ["M86 66C91 58 99 57 104 62C99 72 91 74 86 66Z","M134 66C129 58 121 57 116 62C121 72 129 74 134 66Z"]
def almond_fill():
    # iris dots + a bold upper lid (thin filled crescent)
    return [ell(96,65.5,3.6,3.6),ell(124,65.5,3.6,3.6),"M86 66C91 58 99 57 104 62C99 60 91 62 86 66Z","M134 66C129 58 121 57 116 62C121 60 129 62 134 66Z"]
def eyes(kind):
    if kind=="calm":
        return almond(0)+["M85 54C91 49 98 50 103 55","M135 54C129 49 122 50 117 55","M102 84C107 88 114 88 119 83"],almond_fill()
    if kind=="happy":
        return ["M86 68C89 57 101 57 104 68","M116 68C119 57 131 57 134 68","M86 49C91 45 100 45 104 49","M134 49C129 45 120 45 116 49","M99 83C107 92 113 92 121 83"],[]
    if kind=="sleep":
        return ["M86 62C89 72 101 72 104 62","M116 62C119 72 131 72 134 62","M103 86C108 89 112 89 117 86"],[]
    if kind=="stern":
        return almond(0)+["M85 51L103 56","M135 51L117 56","M103 85H117"],almond_fill()
def arms(): return ["M121 110C147 116 153 144 133 160C123 168 108 166 97 158"]
def robot(kind):
    e,d=eyes(kind)
    head=[rot(x,-5,110,102) for x in [HEAD,EARL,EARR]+e]
    hairs=[rot(x,-5,110,102) for x in [HAIR,BUN]]
    return head+[BODY,BODY2], d, hairs
S={}
def scene(n,col,kind,prop_line,prop_fill,tx=6,ty=6,sc=1.0,front=None,extra=()):
    rb,d,br=robot(kind)
    dots=[rot(x,-5,110,102) for x in d]
    S[n]=dict(col=col,robot=rb,dots=dots,brand=br,prop=prop_line,fill=prop_fill,arms=arms(),extra=list(extra),tx=tx,ty=ty,sc=sc)
H=heart(114,136,.85)
scene("nothing_owed","#B3265B","calm",[H],[H])
CK=circ(114,134,19)
scene("all_kept","#23706B","happy",[CK,"M105 134L112 141L124 125"],[CK],extra=["M174 40V52M168 46H180","M30 70V80M25 75H35","M180 110V118M176 114H184"])
SUN="M94 148A21 21 0 0 1 136 148Z"
scene("clear_sky","#F5A524","calm",[SUN,"M86 148H144","M115 114V124","M135 120L130 127","M95 120L100 127"],[SUN])
PG="M98 116H132V154H98Z"
scene("blank_page","#F5A524","calm",[PG,"M104 128H126","M104 138H118"],[PG])
MN="M122 116A22 22 0 1 0 132 156A17 17 0 0 1 122 116Z"
scene("quiet_day","#2563B8","sleep",[MN],[MN],extra=["M176 50V60M171 55H181","M30 96V104M26 100H34"])
scene("no_one_yet","#2563B8","calm",[],[],extra=[circ(182,90,10),"M164 134C164 116 173 110 182 110C191 110 200 116 200 134"])
scene("stern","#B3265B","stern",[],[])
json.dump(S,open("scenes6.json","w"))
def svg(names,SW=1.6,cols=4):
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
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="500"><rect width="1000" height="500" fill="#F2F8F7"/>{out}</svg>'
open("sheet6.svg","w").write(svg(list(S)))
