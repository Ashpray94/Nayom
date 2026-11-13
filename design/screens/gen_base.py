import json, os, re
L=dict(bg1="#F8FCFB",bg2="#E4F1EF",ink="#12302F",muted="#4B6663",line="rgba(18,48,47,.20)",grid="rgba(18,48,47,.045)",teal="#23706B",onteal="#FFFFFF",amber="#B86E00",amberf="#F5A524",berry="#B3265B",leaf="#3F7A1F",sky="#2563B8",panel="rgba(255,255,255,.65)",glow="rgba(35,112,107,.0)")
D=dict(bg1="#0B1F22",bg2="#050F11",ink="#E8F5F2",muted="#A9C4C0",line="rgba(232,245,242,.22)",grid="rgba(232,245,242,.05)",teal="#5FD0C0",onteal="#04201F",amber="#FFB938",amberf="#FFB938",berry="#FF7AA8",leaf="#9FD66B",sky="#7FB0FF",panel="rgba(255,255,255,.04)",glow="rgba(95,208,192,.16)")
SANS="font-family:'Sora',system-ui,sans-serif"
SERIF="font-family:'Cormorant Garamond',Georgia,serif"
def lab(C,t,col=None,extra=""):
    return f'<div style="font-size:11px;letter-spacing:.18em;text-transform:uppercase;font-weight:400;color:{col or C["muted"]};{extra}">{t}</div>'
def hair(C): return f'<div style="height:1px;background:{C["line"]}"></div>'
def head(C,right):
    return f'<div style="position:absolute;left:24px;right:24px;top:52px;display:flex;justify-content:space-between;align-items:center"><span style="font-size:11px;font-weight:600;letter-spacing:.34em;color:{C["ink"]}">NAYOM</span><span style="display:inline-flex;align-items:center;gap:8px;font-size:11px;letter-spacing:.14em;color:{C["muted"]}"><span style="width:6px;height:6px;border-radius:3px;background:{C["teal"]};box-shadow:0 0 0 3px {C["glow"] if C["glow"]!="rgba(35,112,107,.0)" else "rgba(35,112,107,.18)"}"></span>{right}</span></div>'
def br(C,w,h):
    c=C["ink"]; a=f'stroke="{c}" stroke-width="1.2" fill="none" opacity=".55"'
    return f'<svg width="{w}" height="{h}" viewBox="0 0 {w} {h}" style="position:absolute;left:0;top:0;pointer-events:none" aria-hidden="true"><path d="M0 12V0H12" {a}/><path d="M{w-12} 0H{w}V12" {a}/><path d="M{w} {h-12}V{h}H{w-12}" {a}/><path d="M12 {h}H0V{h-12}" {a}/></svg>'
def cell(C,k,v,col=None,first=False):
    bl="" if first else f"border-left:1px solid {C['line']};"
    return f'<div style="flex:1;padding:2px 0 2px {0 if first else 14}px;{bl}">{lab(C,k)}<div style="font-size:30px;font-weight:200;letter-spacing:.01em;color:{col or C["ink"]};margin-top:6px;line-height:1">{v}</div></div>'
def stats(C,items):
    return f'<div style="display:flex">'+''.join(cell(C,k,v,c,i==0) for i,(k,v,c) in enumerate(items))+'</div>'
def btn(C,t,kind="primary",dim=False):
    if kind=="primary": st=f'background:{C["teal"]};color:{C["onteal"]};border:1px solid {C["teal"]};font-weight:700'
    else: st=f'background:transparent;color:{C["ink"]};border:1px solid {C["line"]};font-weight:400'
    return f'<button style="{SANS};{st};{"opacity:.45;" if dim else ""}height:56px;border-radius:6px;font-size:13px;letter-spacing:.2em;text-transform:uppercase;width:100%">{t}</button>'
def row(C,code,name,sub,status,col,time):
    return f'<div style="display:flex;align-items:center;gap:14px;padding:13px 0;border-top:1px solid {C["line"]}"><span style="font-size:11px;letter-spacing:.1em;color:{C["muted"]};width:42px">{code}</span><div style="flex:1;min-width:0"><div style="font-size:16px;font-weight:400;color:{C["ink"]};line-height:1.25">{name}</div><div style="font-size:12px;font-weight:300;color:{C["muted"]};margin-top:2px">{sub}</div></div><div style="text-align:right"><div style="font-size:11px;letter-spacing:.16em;font-weight:700;color:{col}">{status}</div><div style="font-size:12px;font-weight:300;color:{C["muted"]};margin-top:2px">{time}</div></div></div>'
def tabbar(C,a=0):
    items=''.join(f'<div style="flex:1;text-align:center;font-size:11px;letter-spacing:.18em;text-transform:uppercase;color:{C["ink"] if i==a else C["muted"]};font-weight:{600 if i==a else 400};position:relative;padding-top:2px">{n}{f"<div style=\"position:absolute;left:30%;right:30%;top:-14px;height:2px;background:{C[chr(116)+chr(101)+chr(97)+chr(108)]}\"></div>" if i==a else ""}</div>' for i,n in enumerate(["Today","Promises","People","Notes"]))
    return f'<div style="position:absolute;left:0;right:0;bottom:0;height:78px;border-top:1px solid {C["line"]};background:{C["panel"]};display:flex;align-items:center;padding:0 12px 10px;box-sizing:border-box">{items}</div>'
def timeline(C):
    W=342; x=lambda t:(t-6)/18*W
    tk=''.join(f'<line x1="{x(h):.1f}" x2="{x(h):.1f}" y1="{34 if h%6==0 else 38}" y2="44" stroke="{C["ink"]}" stroke-opacity="{.7 if h%6==0 else .35}"/>' for h in range(6,25))
    lbl=''.join(f'<text x="{x(h):.1f}" y="60" text-anchor="{"start" if h==6 else "end" if h==24 else "middle"}" font-family="Sora" font-size="10" letter-spacing="1.5" fill="{C["muted"]}">{h:02d}</text>' for h in(6,12,18,24))
    now=14+10/60
    past=f'<line x1="0" x2="{x(now):.1f}" y1="44" y2="44" stroke="{C["teal"]}" stroke-width="2"/><line x1="{x(now):.1f}" x2="{W}" y1="44" y2="44" stroke="{C["ink"]}" stroke-opacity=".35"/>'
    kept=f'<circle cx="{x(10+20/60):.1f}" cy="22" r="5" fill="{C["leaf"]}"/>'
    xm=x(12); missed=f'<circle cx="{xm:.1f}" cy="22" r="6" fill="none" stroke="{C["berry"]}" stroke-width="1.6"/><path d="M{xm-2.5:.1f} 19.5l5 5M{xm+2.5:.1f} 19.5l-5 5" stroke="{C["berry"]}" stroke-width="1.6" stroke-linecap="round"/>'
    xd=x(17); due=f'<path d="M{xd:.1f} 15l7 7-7 7-7-7z" fill="{C["amberf"]}"/>'
    xn=x(now); nowm=f'<line x1="{xn:.1f}" x2="{xn:.1f}" y1="8" y2="50" stroke="{C["ink"]}" stroke-width="1.2"/><text x="{xn+5:.1f}" y="12" font-family="Sora" font-size="9" letter-spacing="1.4" fill="{C["ink"]}">NOW 14:10</text>'
    return f'<svg width="{W}" height="68" viewBox="0 0 {W} 68" aria-hidden="true">{tk}{past}{lbl}{kept}{missed}{due}{nowm}</svg>'
def page(title,body,C,w=390,h=844,preview=False):
    ff=''.join(f"@font-face{{font-family:'Cormorant Garamond';font-weight:{wt};font-style:{st};src:url(/home/claude/brand/fonts/node_modules/@fontsource/cormorant-garamond/files/cormorant-garamond-latin-{wt}-{st}.woff2)}}" for wt in(400,500,600,700) for st in("normal","italic"))+''.join(f"@font-face{{font-family:Sora;font-weight:{wt};src:url(/home/claude/brand/fonts/node_modules/@fontsource/sora/files/sora-latin-{wt}-normal.woff2)}}" for wt in(200,300,400,600,700))
    bg=f'background-color:{C["bg2"]};background-image:radial-gradient(120% 60% at 50% -10%,{C["glow"]},transparent 70%),linear-gradient({C["grid"]} 1px,transparent 1px),linear-gradient(90deg,{C["grid"]} 1px,transparent 1px),linear-gradient(180deg,{C["bg1"]},{C["bg2"]});background-size:100% 100%,39px 39px,39px 39px,100% 100%'
    root=f'<div style="width:{w}px;height:{h}px;box-sizing:border-box;{bg};position:relative;overflow:hidden;{SANS};font-weight:300;color:{C["ink"]};letter-spacing:.01em">{body}</div>'
    link='<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500;1,600&family=Sora:wght@200;300;400;600;700&display=swap">'
    if preview: return f'<!doctype html><meta charset="utf-8"><style>{ff}body{{margin:0}}</style>{root}'
    return f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>{title}</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
{link}
<style>
body{{margin:0;font-family:'Sora',system-ui,sans-serif;color:{C['ink']}}}
a{{color:{C['teal']}}}
</style>
</helmet>
{root}
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":{w},"height":{h}}}}}'>
class Component extends DCLogic {{
renderVals() {{ return {{}}; }}
}}
</script>
</body>
</html>
'''
def H(C,t,size=64,ital=False,col=None):
    return f'<div style="{SERIF};font-size:{size}px;font-weight:500;{"font-style:italic;" if ital else ""}line-height:.95;letter-spacing:-.01em;color:{col or C["ink"]}">{t}</div>'
def s_today(C):
    return head(C,"READING")+f'''
<div style="position:absolute;left:24px;right:24px;top:96px">{lab(C,"12 NOV 2026 · THURSDAY")}<div style="margin-top:8px">{H(C,"Today",44,True)}</div></div>
<div style="position:absolute;left:24px;right:24px;top:178px;height:112px;padding:16px 18px;box-sizing:border-box">{br(C,342,112)}
{lab(C,"Next · Meena · Final banner file")}<div style="display:flex;align-items:baseline;gap:10px;margin-top:10px"><span style="font-size:11px;letter-spacing:.2em;color:{C["amber"]}">T–</span><span style="font-size:48px;font-weight:200;letter-spacing:.02em;line-height:1;color:{C["ink"]}">02:50:12</span></div><div style="font-size:12px;font-weight:300;color:{C["muted"]};margin-top:8px;letter-spacing:.04em">Due 17:00 · Meena</div></div>
<div style="position:absolute;left:24px;top:306px">{timeline(C)}</div>
<div style="position:absolute;left:24px;right:24px;top:392px">{stats(C,[("Kept","2",C["leaf"]),("Missed","1",C["berry"]),("Due","3",C["amber"]),("Week","9/11",None)])}</div>
<div style="position:absolute;left:24px;right:24px;top:466px">{row(C,"P-14","Final banner file","Meena","DUE","" + C["amber"],"17:00")}{row(C,"P-12","Review notes for the logo","Raj","MISSED",C["berry"],"12:00")}{row(C,"P-11","Invoice draft","Ravi","KEPT",C["leaf"],"10:20")}<div style="border-top:1px solid {C["line"]}"></div></div>
<div style="position:absolute;left:24px;right:96px;top:660px">{lab(C,"Care",C["sky"])}<div style="font-size:15px;font-weight:300;margin-top:4px">Eat something. Lunch was 70 min ago.</div></div>
<div style="position:absolute;right:24px;bottom:98px;width:56px;height:56px;border-radius:28px;border:1px solid {C["teal"]};display:flex;align-items:center;justify-content:center;background:{C["panel"]}"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="{C["teal"]}" stroke-width="1.6" stroke-linecap="round"><rect x="9" y="3.5" width="6" height="11" rx="3"/><path d="M5.5 11.5a6.5 6.5 0 0 0 13 0M12 18v3"/></svg></div>
{tabbar(C,0)}'''
def ring(C,n=3,pct=.6):
    return f'<svg width="92" height="92" viewBox="0 0 92 92"><circle cx="46" cy="46" r="40" fill="none" stroke="{C["line"]}" stroke-width="1.5"/><circle cx="46" cy="46" r="40" fill="none" stroke="{C["teal"]}" stroke-width="2.5" stroke-dasharray="{251*pct:.0f} 251" transform="rotate(-90 46 46)" stroke-linecap="round"/><text x="46" y="57" text-anchor="middle" font-family="Sora" font-weight="200" font-size="34" fill="{C["ink"]}">{n}</text></svg>'
def s_gate(C):
    return head(C,"HOLD")+f'''
<div style="position:absolute;left:24px;right:24px;top:110px">{lab(C,"Request · Ravi")}<div style="margin-top:14px">{H(C,"Hold on.",72)}</div><div style="font-size:18px;font-weight:300;margin-top:16px">Banner tweak · Today</div></div>
<div style="position:absolute;right:24px;top:116px">{ring(C)}</div>
<div style="position:absolute;left:24px;right:24px;top:352px">{row(C,"LOAD","3 promises due in 48 h","Already on your plate","3",C["amber"],"48 h")}{row(C,"RISK","Meena's file, 17:00","Saying yes pushes it out","AT RISK",C["berry"],"17:00")}<div style="border-top:1px solid {C["line"]}"></div></div>
<div style="position:absolute;left:24px;right:24px;bottom:50px;display:flex;flex-direction:column;gap:10px">{btn(C,"Yes","ghost",True)}{btn(C,"Later","ghost",True)}{btn(C,"No")}<a href="#why" style="color:{C["teal"]};text-align:center;font-size:11px;letter-spacing:.18em;text-transform:uppercase;margin-top:6px">Why am I seeing this</a></div>'''
def s_slip(C):
    return head(C,"STATUS")+f'''
<div style="position:absolute;left:24px;right:24px;top:110px">{lab(C,"Promise · Meena · Final banner file",C["berry"])}<div style="margin-top:14px">{H(C,"Missed.",72)}</div></div>
<div style="position:absolute;left:24px;right:24px;top:252px;padding:16px 18px;box-sizing:border-box;height:104px">{br(C,342,104)}{lab(C,"Late")}<div style="display:flex;align-items:baseline;gap:10px;margin-top:10px"><span style="font-size:11px;letter-spacing:.2em;color:{C["berry"]}">T+</span><span style="font-size:44px;font-weight:200;letter-spacing:.02em;line-height:1;color:{C["berry"]}">00:42:11</span></div></div>
<div style="position:absolute;left:24px;right:24px;top:380px"><div style="display:flex;justify-content:space-between;font-size:11px;letter-spacing:.14em;color:{C["muted"]}"><span>DUE 17:00</span><span>NOW 17:42</span></div><div style="height:2px;background:{C["line"]};margin-top:10px;position:relative"><div style="position:absolute;left:0;width:62%;top:0;bottom:0;background:{C["berry"]}"></div></div></div>
<div style="position:absolute;left:24px;right:24px;top:440px">{row(C,"LOG","No file saved or sent","Since 09:15 · 2 events","EVIDENCE",C["muted"],"17:42")}{row(C,"SAID","“I'll send it by five.”","Voice note · 09:12","YOURS",C["teal"],"09:12")}<div style="border-top:1px solid {C["line"]}"></div></div>
<div style="position:absolute;left:24px;right:24px;bottom:50px;display:flex;flex-direction:column;gap:10px">{btn(C,"Fix it by…")}{btn(C,"Release","ghost")}<a href="#wrong" style="color:{C["teal"]};text-align:center;font-size:11px;letter-spacing:.18em;text-transform:uppercase;margin-top:6px">Wrong</a></div>'''
def s_eve(C):
    bars=[40,64,52,70,34,58,46]; days="MTWTFSS"
    bs=''.join(f'<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:8px"><div style="height:74px;display:flex;align-items:flex-end"><div style="width:6px;height:{h}px;background:{C["teal"] if i==3 else C["line"]}"></div></div><span style="font-size:10px;letter-spacing:.14em;color:{C["ink"] if i==3 else C["muted"]}">{days[i]}</span></div>' for i,h in enumerate(bars))
    return head(C,"DAY COMPLETE")+f'''
<div style="position:absolute;left:24px;right:24px;top:100px">{lab(C,"12 NOV 2026")}<div style="margin-top:8px">{H(C,"Today",44,True)}</div></div>
<div style="position:absolute;left:24px;right:24px;top:182px;height:128px;padding:16px 18px;box-sizing:border-box">{br(C,342,128)}{lab(C,"Kept today")}<div style="display:flex;align-items:baseline;gap:8px;margin-top:8px"><span style="font-size:72px;font-weight:200;line-height:1">2</span><span style="font-size:28px;font-weight:200;color:{C["muted"]}">/ 3</span></div></div>
<div style="position:absolute;left:24px;right:24px;top:334px">{stats(C,[("Missed","1",C["berry"]),("Moved","1",C["teal"]),("Reflex yes","1",C["sky"])])}</div>
<div style="position:absolute;left:24px;right:24px;top:424px">{lab(C,"Week · kept 9 of 11")}<div style="display:flex;margin-top:12px">{bs}</div><div style="font-size:11px;letter-spacing:.14em;color:{C["muted"]};margin-top:12px">0 DON'T MATCH</div></div>
<div style="position:absolute;left:24px;right:24px;top:612px;border-top:1px solid {C["line"]};padding-top:14px">{lab(C,"Care",C["sky"])}<div style="font-size:16px;font-weight:300;margin-top:4px">Phone down. Sleep well.</div></div>
<div style="position:absolute;left:24px;right:24px;bottom:44px">{btn(C,"Done")}</div>'''
def s_setup(C):
    rows=[("Restricted settings","GO",C["leaf"]),("Accessibility","NOW",C["teal"]),("Notifications","PENDING",C["muted"]),("Overlay","PENDING",C["muted"]),("Usage","PENDING",C["muted"]),("Vault","PENDING",C["muted"])]
    rs=''
    for i,(n,s,c) in enumerate(rows):
        now=s=="NOW"
        rs+=f'<div style="padding:{"16px" if now else "13px"} 0;border-top:1px solid {C["line"]}"><div style="display:flex;justify-content:space-between;align-items:center"><span style="font-size:{18 if now else 15}px;font-weight:{400 if now else 300};color:{C["ink"] if now else C["muted"]}"><span style="font-size:11px;letter-spacing:.1em;color:{C["muted"]};margin-right:14px">0{i+1}</span>{n}</span><span style="font-size:11px;letter-spacing:.16em;font-weight:700;color:{c}">{s}</span></div>'+(f'<div style="font-size:14px;font-weight:300;line-height:1.5;margin:10px 0 0 32px;color:{C["ink"]}">Lets me see what you type and what is on screen. Blocked apps are never read.</div>' if now else '')+'</div>'
    return head(C,"STEP 2 OF 6")+f'''
<div style="position:absolute;left:24px;right:24px;top:110px">{lab(C,"Systems check")}<div style="margin-top:14px">{H(C,"Let's get<br><i>set up.</i>",56)}</div></div>
<div style="position:absolute;left:24px;right:24px;top:322px">{rs}<div style="border-top:1px solid {C["line"]}"></div></div>
<div style="position:absolute;left:24px;right:24px;bottom:50px;display:flex;flex-direction:column;gap:10px">{btn(C,"Open settings")}<a href="#back" style="color:{C["teal"]};text-align:center;font-size:11px;letter-spacing:.18em;text-transform:uppercase;margin-top:6px">Back</a></div>'''
S=[("Main","Today",s_today),("Gate","Gate",s_gate),("Slip","Slip",s_slip),("Evening","Evening",s_eve),("Setup","Setup",s_setup)]
os.makedirs("project",exist_ok=True);os.makedirs("prev",exist_ok=True)
boards={};order=[];notes={}
for ti,(tn,C) in enumerate((("",L),("D",D))):
    for i,(n,t,f) in enumerate(S):
        fn=f"{n}{tn}.dc.html"; b=f(C)
        open(f"project/{fn}","w").write(page(t,b,C)); open(f"prev/{fn}.html","w").write(page(t,b,C,preview=True))
        boards[fn]={"x":i*470,"y":ti*940,"w":390,"h":844,"title":f"{t} · {'dark' if tn else 'light'}"}; order.append(fn)
idx={"v":3,"createdOnFiles":{"v":1,"at":"2026-11-13T01:00:00Z"},"title":"Nayom: key screens (HUD)","launch":{"view":"canvas"},"pages":[],"boards":boards,"order":order,"notes":{"t1":{"x":0,"y":-300,"text":"Nayom key screens, mission-control style","kind":"title1","maxW":2260}},"designSystems":[]}
open("project/canvas.json","w").write(json.dumps(idx,indent=1))
