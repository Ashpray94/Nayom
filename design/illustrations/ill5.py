import math,json,re
INK="#12302F"; TEAL="#23706B"
def rr(x,y,w,h,r): return f"M{x+r} {y}H{x+w-r}A{r} {r} 0 0 1 {x+w} {y+r}V{y+h-r}A{r} {r} 0 0 1 {x+w-r} {y+h}H{x+r}A{r} {r} 0 0 1 {x} {y+h-r}V{y+r}A{r} {r} 0 0 1 {x+r} {y}Z"
def circ(cx,cy,r): return f"M{cx-r} {cy}A{r} {r} 0 1 1 {cx+r} {cy}A{r} {r} 0 1 1 {cx-r} {cy}Z"
def heart(cx,cy,k=1.0):
    p=lambda x,y:f"{cx+x*k:.1f} {cy+y*k:.1f}"
    return f"M{p(0,16)}C{p(-26,0)} {p(-20,-20)} {p(-6,-19)}C{p(-2,-19)} {p(0,-16)} {p(0,-16)}C{p(0,-16)} {p(2,-19)} {p(6,-19)}C{p(20,-20)} {p(26,0)} {p(0,16)}Z"
# Character: big round head, hair cap with a bun, big soft eyes with a highlight dot, ear lights, small pod body. Head centre (120,104) r 66.
HEAD=circ(120,104,66)
BUN="M92 44C94 24 108 14 124 14C142 14 154 28 150 46"             # bun, open arc (head hides the rest)
HAIR="M56 96C58 62 84 40 118 40C150 40 176 60 182 92C170 76 154 72 138 80C118 92 84 92 56 96Z"  # cap, wavy lower edge
EARL="M52 92C47 104 47 116 52 128"; EARR="M188 92C193 104 193 116 188 128"
def eyes(kind):
    if kind=="calm":
        return [rr(66,98,42,36,16),rr(132,98,42,36,16)],[circ(95,109,4),circ(161,109,4)]
    if kind=="happy":
        return ["M70 124C76 102 100 102 106 124","M134 124C140 102 164 102 170 124"],[]
    if kind=="sleep":
        return ["M70 108C76 130 100 130 106 108","M134 108C140 130 164 130 170 108"],[]
    if kind=="stern":
        return ["M66 106H108V118C108 127 101 134 92 134H82C73 134 66 127 66 118Z","M132 106H174V118C174 127 167 134 158 134H148C139 134 132 127 132 118Z"],[circ(95,119,4),circ(161,119,4)]
POD="M98 178C98 172 108 170 120 170C132 170 142 172 142 178C148 198 144 222 136 234H104C96 222 92 198 98 178Z"
ARMS=["M96 184C80 192 76 212 80 228","M144 184C160 192 164 212 160 228"]
def robot(kind="calm"):
    e,d=eyes(kind)
    line=[HEAD,BUN,EARL,EARR,HAIR,POD]+ARMS+e
    return line,d,[HAIR,BUN_FILL]
BUN_FILL="M92 44C94 24 108 14 124 14C142 14 154 28 150 46Z"
S={}
def scene(n,col,kind,line,fill,tx,ty,sc=.74,dots=()):
    l,d,brand=robot(kind); S[n]=dict(col=col,robot=l,dots=d+list(dots),brand=brand,line=line,fill=fill,tx=tx,ty=ty,sc=sc)
sun="M158 170A36 36 0 0 1 230 170Z"
scene("clear_sky","#F5A524","calm",["M148 170H238","M194 118V130","M224 128L217 136","M164 128L171 136"],[sun],-8,6)
ch=circ(184,58,26)
scene("all_kept","#23706B","happy",[ch,"M173 58L181 66L196 48","M222 24V34M217 29H227","M24 44V54M19 49H29"],[ch],-14,14)
h=heart(184,138)
scene("nothing_owed","#B3265B","calm",["M150 206C166 208 174 202 180 192","M172 186C172 196 178 200 186 200C194 200 200 196 200 186",h],[h],-18,8)
def av(cx,cy): return [circ(cx,cy,11),f"M{cx-22} {cy+42}C{cx-22} {cy+22} {cx-11} {cy+18} {cx} {cy+18}C{cx+11} {cy+18} {cx+22} {cy+22} {cx+22} {cy+42}"]
scene("no_one_yet","#2563B8","calm",av(184,100)+av(222,134),[circ(184,100,11)],-34,14)
page="M170 136H228V224H170Z"
scene("blank_page","#F5A524","calm",[page,"M182 156H214","M182 170H204","M150 212C160 214 168 208 174 200","M174 200L200 176L208 184L182 208Z"],[page],-34,2)
moon="M200 44A30 30 0 1 0 220 100A24 24 0 0 1 200 44Z"
scene("quiet_day","#2563B8","sleep",[moon,"M154 32V42M149 37H159","M232 130V138M228 134H236"],[moon],-22,14)
scene("stern","#B3265B","stern",[],[],34,10)
scene("hello","#23706B","calm",[],[],34,10,dots=())
json.dump(S,open("scenes5.json","w"))
def svg(names,SW=1.6,cols=4):
    out=""
    for i,n in enumerate(names):
        s=S[n]; x=(i%cols)*250; y=(i//cols)*250
        g=f'<g transform="translate({x+5},{y+5})" fill="none" stroke="{INK}" stroke-width="{SW}" stroke-linecap="round" stroke-linejoin="round">'
        for d in s["fill"]: g+=f'<path d="{d}" fill="{s["col"]}" fill-opacity=".38" stroke="none"/>'
        g+=f'<g transform="translate({s["tx"]},{s["ty"]}) scale({s["sc"]})" stroke-width="{SW/s["sc"]}">'
        for d in s["brand"]: g+=f'<path d="{d}" fill="{TEAL}" fill-opacity=".45" stroke="none"/>'
        g+="".join(f'<path d="{d}"/>' for d in s["robot"])+"".join(f'<path d="{d}" fill="{INK}" stroke="none"/>' for d in s["dots"])+'</g>'
        for d in s["line"]: g+=f'<path d="{d}"/>'
        out+=g+"</g>"
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="500"><rect width="1000" height="500" fill="#F2F8F7"/>{out}</svg>'
open("sheet5.svg","w").write(svg(list(S)))
