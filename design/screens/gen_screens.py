import json, os, re, io, contextlib
with contextlib.redirect_stdout(io.StringIO()):
    import gen3
from gen3 import page,btn,H,lab,SANS,SERIF,L,D
L=dict(L,grid="transparent",glow="rgba(0,0,0,0)",card="#FFFFFF",cardx="box-shadow:0 10px 32px rgba(18,48,47,.07);",ring="rgba(35,112,107,.14)")
D=dict(D,grid="transparent",glow="rgba(95,208,192,.10)",card="rgba(255,255,255,.055)",cardx="box-shadow:inset 0 0 0 1px rgba(232,245,242,.09);",ring="rgba(95,208,192,.2)")
def ico(n,size,col):
    t=open(f"/home/claude/icons/svg/{n}.svg").read()
    inner=t[t.index(">")+1:t.rindex("</svg>")]
    return f'<svg width="{size}" height="{size}" viewBox="0 0 48 48" fill="none" stroke="{col}" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" style="color:{col};flex:none">{inner}</svg>'
def tile(n,col,size=44):
    return f'<span style="width:{size}px;height:{size}px;border-radius:12px;background:{col}22;display:inline-flex;align-items:center;justify-content:center;flex:none">{ico(n,size*0.58,col)}</span>'
def head(C,right):
    return f'<div style="position:absolute;left:28px;right:28px;top:54px;display:flex;justify-content:space-between;align-items:center"><span style="font-size:11px;font-weight:600;letter-spacing:.34em;color:{C["ink"]}">NAYOM</span><span style="display:inline-flex;align-items:center;gap:8px;font-size:11px;letter-spacing:.14em;color:{C["muted"]}"><span style="width:6px;height:6px;border-radius:3px;background:{C["teal"]}"></span>{right}</span></div>'
def card(C,inner,pad=24,extra=""):
    pad=f"{pad}px" if isinstance(pad,int) else pad
    return f'<div style="background:{C["card"]};{C["cardx"]}border-radius:22px;padding:{pad};{extra}">{inner}</div>'
def row(C,name,sub,status,col,time,first=False,ic=None):
    bt="" if first else f"border-top:1px solid {C['line']};"
    return f'<div style="display:flex;align-items:center;justify-content:space-between;padding:16px 0;{bt}"><div style="display:flex;gap:14px;align-items:center">{tile(ic,col) if ic else ""}<div><div style="font-size:16px;font-weight:400;line-height:1.25">{name}</div><div style="font-size:13px;font-weight:400;color:{C["muted"]};margin-top:3px">{sub}</div></div></div><div style="text-align:right"><div style="font-size:11px;letter-spacing:.16em;font-weight:700;color:{col}">{status}</div><div style="font-size:13px;font-weight:400;color:{C["muted"]};margin-top:3px">{time}</div></div></div>'
def tabbar(C,a=0):
    ids=["today","promises","people","notes"]
    it=''.join(f'<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:6px;color:{C["teal"] if i==a else C["muted"]}">{ico(n,26,C["teal"] if i==a else C["muted"]).replace("<svg ","<svg ",1) if i!=a else ico(n+"-active",26,C["teal"])}<span style="font-size:10px;letter-spacing:.16em;text-transform:uppercase;font-weight:{600 if i==a else 400}">{n.replace("today","Today").replace("promises","Promises").replace("people","People").replace("notes","Notes")}</span></div>' for i,n in enumerate(ids))
    return f'<div style="position:absolute;left:0;right:0;bottom:0;height:84px;display:flex;align-items:flex-start;padding:12px 12px 0;box-sizing:border-box;border-top:1px solid {C["line"]}">{it}</div>'
def links(C,t,h): return f'<a href="#{h}" style="color:{C["teal"]};text-align:center;font-size:11px;letter-spacing:.18em;text-transform:uppercase;margin-top:6px">{t}</a>'
def s_today(C):
    return head(C,"Reading")+f'''
<div style="position:absolute;left:28px;top:104px">{H(C,"Thursday",46,True)}<div style="margin-top:10px">{lab(C,"12 November")}</div></div>
<div style="position:absolute;left:28px;right:28px;top:208px">{card(C,f"""<div style="display:flex;align-items:center;gap:8px">{ico("clock",20,C["amber"])}{lab(C,"Next · Meena")}</div><div style="font-size:22px;font-weight:400;margin-top:12px">Final banner file</div><div style="font-size:56px;font-weight:200;letter-spacing:.01em;line-height:1;margin-top:22px">02:50:12</div><div style="font-size:13px;font-weight:400;color:{C["muted"]};margin-top:12px">Due 17:00</div>""",28)}</div>
<div style="position:absolute;left:28px;right:28px;top:470px">{card(C,row(C,"Review notes for the logo","Raj · due 12:00","MISSED",C["berry"],"",True,"missed")+row(C,"Invoice draft","Ravi · sent 10:20","KEPT",C["leaf"],"",False,"check"),"4px 20px")}</div>
<div style="position:absolute;left:28px;right:28px;bottom:100px;display:flex;justify-content:center"><button style="font-family:Sora;color:{C["ink"]};background:{C["teal"]};color:{C["onteal"]};border:1px solid {C["teal"]};height:56px;border-radius:6px;font-size:13px;letter-spacing:.2em;text-transform:uppercase;padding:0 28px;display:flex;align-items:center;gap:12px;font-weight:700">{ico("mic",24,C["onteal"])}Debrief</button></div>
{tabbar(C,0)}'''
def s_gate(C):
    return head(C,"Hold")+f'''
<div style="position:absolute;left:28px;right:28px;top:120px">{H(C,"Hold on.",76)}<div style="font-size:18px;font-weight:400;margin-top:20px;line-height:1.4">Ravi asks: Banner tweak, today.</div></div>
<div style="position:absolute;left:28px;right:28px;top:330px">{card(C,f"""<div style="display:flex;justify-content:space-between;align-items:baseline"><span style="font-size:16px">3 promises due in 48 h</span></div><div style="height:1px;background:{C["line"]};margin:16px 0"></div><div style="display:flex;gap:14px;align-items:center">{tile("missed",C["berry"])}<div><div style="font-size:16px">Pushes out Meena's file</div><div style="font-size:11px;letter-spacing:.16em;font-weight:700;color:{C["berry"]};margin-top:6px">17:00 · AT RISK</div></div></div>""",24)}</div>
<div style="position:absolute;left:28px;right:28px;bottom:52px;display:flex;flex-direction:column;gap:12px"><div style="margin-bottom:6px">{lab(C,"Yes and Later unlock in 3 s")}<div style="height:2px;background:{C["line"]};margin-top:10px"><div style="width:40%;height:100%;background:{C["teal"]}"></div></div></div>{btn(C,"Yes","ghost",True)}{btn(C,"Later","ghost",True)}{btn(C,"No")}{links(C,"Why am I seeing this","why")}</div>'''
def s_slip(C):
    return head(C,"Missed")+f'''
<div style="position:absolute;left:28px;right:28px;top:120px">{H(C,"Missed.",76)}<div style="font-size:18px;font-weight:400;margin-top:20px;line-height:1.45">Meena's final banner file was due at 17:00.</div></div>
<div style="position:absolute;left:28px;right:28px;top:320px">{card(C,f"""<div style="display:flex;align-items:center;gap:8px">{ico("missed",20,C["berry"])}{lab(C,"Late")}</div><div style="font-size:56px;font-weight:200;line-height:1;margin-top:14px;color:{C["berry"]}">42<span style="font-size:20px;font-weight:400;margin-left:8px;color:{C["muted"]}">min</span></div><div style="height:1px;background:{C["line"]};margin:22px 0 16px"></div><div style="font-size:14px;font-weight:400;color:{C["muted"]};line-height:1.5">No file saved or sent since 09:15.</div>""",28)}</div>
<div style="position:absolute;left:28px;right:28px;bottom:52px;display:flex;flex-direction:column;gap:12px">{btn(C,"Fix it by…")}{btn(C,"Release","ghost")}{links(C,"Wrong","wrong")}</div>'''
def s_eve(C):
    return head(C,"Day complete")+f'''
<div style="position:absolute;left:28px;top:104px">{H(C,"Today",46,True)}<div style="margin-top:10px">{lab(C,"12 November")}</div></div>
<div style="position:absolute;left:28px;right:28px;top:208px">{card(C,f"""<div style="display:flex;align-items:center;gap:8px">{ico("check",20,C["leaf"])}{lab(C,"Kept")}</div><div style="display:flex;align-items:baseline;gap:10px;margin-top:12px"><span style="font-size:88px;font-weight:200;line-height:1">2</span><span style="font-size:28px;font-weight:200;color:{C["muted"]}">of 3</span></div><div style="height:2px;background:{C["line"]};margin-top:24px"><div style="width:82%;height:100%;background:{C["teal"]}"></div></div><div style="font-size:13px;font-weight:400;color:{C["muted"]};margin-top:10px">9 of 11 this week</div>""",28)}</div>
<div style="position:absolute;left:28px;right:28px;top:520px">{card(C,"<div style='display:flex'>"+''.join(f'<div style="flex:1">{lab(C,k)}<div style="font-size:28px;font-weight:200;margin-top:8px;color:{c}">{v}</div></div>' for k,v,c in (("Missed","1",C["berry"]),("Moved","1",C["ink"]),("Reflex yes","1",C["ink"])))+"</div>",20)}</div>
<div style="position:absolute;left:28px;right:28px;bottom:44px;display:flex;flex-direction:column;gap:20px"><div style="font-size:18px;font-weight:400">Phone down.</div>{btn(C,"Done")}</div>'''
def s_setup(C):
    seg=''.join(f'<div style="flex:1;height:3px;border-radius:2px;background:{C["teal"] if i<2 else C["line"]}"></div>' for i in range(6))
    return head(C,"Step 2 of 6")+f'''
<div style="position:absolute;left:28px;right:28px;top:96px;display:flex;gap:6px">{seg}</div>
<div style="position:absolute;left:28px;right:28px;top:150px">{H(C,"Accessibility",56)}<div style="font-size:18px;font-weight:400;line-height:1.5;margin-top:24px">Lets me see what you type and what is on screen.</div></div>
<div style="position:absolute;left:28px;right:28px;top:380px">{card(C,f"""<div style="display:flex;align-items:center;gap:8px">{ico("shield",20,C["teal"])}{lab(C,"Never read")}</div><div style="font-size:16px;font-weight:400;line-height:1.5;margin-top:10px">Banking, OTP, authenticator and password apps.</div>""",24)}</div>
<div style="position:absolute;left:28px;right:28px;bottom:52px;display:flex;flex-direction:column;gap:12px">{btn(C,"Open settings")}{links(C,"Back","back")}</div>'''
S=[("Main","Today",s_today),("Gate","Gate",s_gate),("Slip","Slip",s_slip),("Evening","Evening",s_eve),("Setup","Setup",s_setup)]
boards={};order=[]
for ti,(tn,C) in enumerate((("",L),("D",D))):
    for i,(n,t,f) in enumerate(S):
        fn=f"{n}{tn}.dc.html"; b=f(C)
        open(f"project/{fn}","w").write(page(t,b,C).replace("font-weight:400;color","font-weight:400;color")); open(f"prev/{fn}.html","w").write(page(t,b,C,preview=True).replace("font-weight:400;color","font-weight:400;color"))
        boards[fn]={"x":i*470,"y":ti*940,"w":390,"h":844,"title":f"{t} · {'dark' if tn else 'light'}"}; order.append(fn)
idx=json.load(open("project/canvas.json")); idx["boards"]=boards; idx["order"]=order
json.dump(idx,open("project/canvas.json","w"),indent=1)
