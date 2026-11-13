import json
INK="#12302F"
SC=.8; TX,TY=22,34   # robot placement inside 240 scene
HEAD="M120 30C158 30 180 60 180 96C180 134 156 158 120 158C84 158 60 134 60 96C60 60 82 30 120 30Z"
VISOR="M82 92C82 78 93 70 109 70H131C147 70 158 78 158 92C158 106 147 116 131 116H109C93 116 82 106 82 92Z"
def eyes(kind,dx=0):
    if kind=="calm":  return [f"M{104+dx} 86V98",f"M{136+dx} 86V98",f"M{113+dx} 105Q{120+dx} 110 {127+dx} 105"]
    if kind=="happy": return [f"M{97+dx} 96Q{104+dx} 86 {111+dx} 96",f"M{129+dx} 96Q{136+dx} 86 {143+dx} 96",f"M{112+dx} 105Q{120+dx} 112 {128+dx} 105"]
    if kind=="sleep": return ["M97 92Q104 99 111 92","M129 92Q136 99 143 92","M115 106H125"]
def body(arm=None):
    P=["M56 232C58 192 86 174 120 174C154 174 182 192 184 232","M106 158V174M134 158V174","M52 86V108M188 86V108"]
    return P
def robot(kind="calm",dx=0): return [HEAD,VISOR]+eyes(kind,dx)+body()
S={}
def scene(name,col,robot_lines,line,fill,tx=TX,ty=TY): S[name]=dict(col=col,robot=robot_lines,line=line,fill=fill,tx=tx,ty=ty)
# 1 clear sky: robot (left) watches a sunrise (right)
sun="M150 150A40 40 0 0 1 230 150Z"
scene("clear_sky","#F5A524",robot("calm",4),["M140 150H236","M190 88V100","M222 100L214 108","M158 100L166 108"],[sun],tx=-4,ty=34)
# helpers
def circ(cx,cy,r): return f"M{cx-r} {cy}A{r} {r} 0 1 1 {cx+r} {cy}A{r} {r} 0 1 1 {cx-r} {cy}Z"
def heart(cx,cy,k=1.0):
    def p(x,y): return f"{cx+x*k:.1f} {cy+y*k:.1f}"
    return f"M{p(0,16)}C{p(-26,0)} {p(-20,-20)} {p(-6,-19)}C{p(-2,-19)} {p(0,-16)} {p(0,-16)}C{p(0,-16)} {p(2,-19)} {p(6,-19)}C{p(20,-20)} {p(26,0)} {p(0,16)}Z"
# 2 all kept: happy, big check badge + sparkles
scene("all_kept","#23706B",robot("happy"),[circ(176,52,30),"M162 52L172 62L190 40","M218 18V30M212 24H224","M24 30V40M19 35H29","M214 100V108M210 104H218"],[circ(176,52,30)],tx=-14,ty=40)
# 3 nothing owed: arm offers a heart on an open palm
h=heart(178,138,1.0)
scene("nothing_owed","#B3265B",robot("calm"),["M134 196C150 198 158 192 164 182","M160 178C160 190 168 194 178 194C188 194 196 190 196 178",h],[h],tx=-14,ty=34)
# 4 no one yet: two empty avatars
def avatar(cx,cy): return [circ(cx,cy,12),f"M{cx-24} {cy+48}C{cx-24} {cy+26} {cx-12} {cy+20} {cx} {cy+20}C{cx+12} {cy+20} {cx+24} {cy+26} {cx+24} {cy+48}"]
scene("no_one_yet","#2563B8",robot("calm"),avatar(176,92)+avatar(222,122),[circ(176,92,12)],tx=-34,ty=40)
# 5 blank page + pencil in hand
page="M164 120H224V212H164Z"
scene("blank_page","#F5A524",robot("calm"),[page,"M176 140H210","M176 154H200","M138 196C150 200 160 196 168 186","M168 186L196 160L204 168L176 194Z","M168 186L166 198L176 194"],[page],tx=-30,ty=24)
# 6 quiet day
moon="M200 40A30 30 0 1 0 220 96A24 24 0 0 1 200 40Z"
scene("quiet_day","#2563B8",robot("sleep"),[moon,"M150 30V40M145 35H155","M232 126V134M228 130H236","M210 150H224L210 166H224"],[moon],tx=-20,ty=40)
json.dump(S,open("scenes.json","w"))
def svg(names,cols=3):
    out=""
    for i,n in enumerate(names):
        s=S[n]; x=(i%cols)*260; y=(i//cols)*260
        g=f'<g transform="translate({x+10},{y+10})" fill="none" stroke="{INK}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">'
        for d in s["fill"]: g+=f'<path d="{d}" fill="{s["col"]}" fill-opacity=".38" stroke="none"/>'
        g+=f'<g transform="translate({s["tx"]},{s["ty"]}) scale({SC})" stroke-width="{2.5/SC}">'+"".join(f'<path d="{d}"/>' for d in s["robot"])+'</g>'
        for d in s["line"]: g+=f'<path d="{d}"/>'
        g+="</g>"; out+=g
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="780" height="520"><rect width="780" height="520" fill="#F2F8F7"/>{out}</svg>'
open("sheet.svg","w").write(svg(list(S)))
