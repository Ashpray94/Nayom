import os
SW=1.5
def svg(body,fill=False,size=48,col="currentColor"):
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 48 48" fill="none" stroke="{col}" stroke-width="{SW}" stroke-linecap="round" stroke-linejoin="round">{body}</svg>'
def F(a): return f' fill="{{c}}" fill-opacity=".16"' if a else ''
def icons(active=False):
    f=' fill="currentColor" fill-opacity=".16"' if active else ''
    return {
# Phosphor sun-horizon + Solar double horizon: the day beginning
"today":f'<path d="M12 33A12 12 0 0 1 36 33Z"{f}/><path d="M5 33H43M12 39.5H36M24 8.5V14M9.5 17.5L13.5 21.5M38.5 17.5L34.5 21.5"/>',
# Solar/Phosphor hand-heart: a promise offered with care
"promises":f'<path d="M24 25C11 17 13 6.5 19.5 7C21.8 7.2 23.4 8.5 24 10.3C24.6 8.5 26.2 7.2 28.5 7C35 6.5 37 17 24 25Z"{f}/><path d="M6.5 30V42"/><path d="M12 31H19C23.5 31 26 34.5 31 32.5L37.8 28A2.9 2.9 0 0 1 42 32.3C36 38.5 30 41.5 24 41.5H12"/>',
# Phosphor users: me in front, another half behind
"people":f'<circle cx="17.5" cy="16.5" r="7"{f}/><path d="M5.5 41C5.5 32 10.5 28 17.5 28C24.5 28 29.5 32 29.5 41"/><path d="M31 10.4A6.5 6.5 0 0 1 31.5 23.2"/><path d="M36 28.5C40.5 29.5 43 33 43 40"/>',
# Phosphor note-pencil: a note being written
"notes":f'<path d="M33 8.5H12.5A5.5 5.5 0 0 0 7 14V36.5A5.5 5.5 0 0 0 12.5 42H34.5A5.5 5.5 0 0 0 40 36.5V21"{f}/><path d="M15 21H22M15 28H27M15 35H21"/><path d="M26 26L27 20L38 9C39.5 7.5 41.8 7.5 43.2 9C44.7 10.5 44.7 12.8 43.2 14.2L32.5 25Z"/><path d="M35.5 11.5L40.5 16.5"/>',
# Solar microphone: grille lines on the capsule
"mic":f'<rect x="16.5" y="4.5" width="15" height="25" rx="7.5"{f}/><path d="M16.5 12H22M16.5 18H22"/><path d="M10 22C10 30.5 16 36 24 36C32 36 38 30.5 38 22M24 36V43.5M18 43.5H30"/>',
# Phosphor check-circle
"check":'<circle cx="24" cy="24" r="18"/><path d="M15.5 25L21.5 31L33 18"/>',
# Phosphor warning-circle
"missed":'<circle cx="24" cy="24" r="18"/><path d="M24 14V26.5"/><path d="M24 33.2V33.4" stroke-width="2.6"/>',
# Phosphor timer + remaining-time wedge
"clock":'<circle cx="24" cy="27" r="16"/><path d="M24 27V11A16 16 0 0 1 40 27Z" fill="currentColor" fill-opacity=".16" stroke="none"/><path d="M18.5 5.5H29.5M24 5.5V11M24 27L31 20"/>',
# shield with keyhole
"shield":'<path d="M24 4.5C29 8 35 9.5 40 9.5V23C40 33.5 33 40 24 43.5C15 40 8 33.5 8 23V9.5C13 9.5 19 8 24 4.5Z"/><circle cx="24" cy="21.5" r="3.6"/><path d="M24 25V31.5"/>',
}
os.makedirs("svg",exist_ok=True)
for k,b in icons().items(): open(f"svg/{k}.svg","w").write(svg(b))
for k,b in icons(True).items():
    if k in("today","promises","people","notes"): open(f"svg/{k}-active.svg","w").write(svg(b))
def cell(k,b,size,col):
    return f'<div style="color:{col}">{svg(b,False,size)}</div>'
names=list(icons().keys())
def panel(bg,ink,muted,line,teal,dark=False):
    I=icons();A=icons(True)
    big=''.join(f'<div style="display:flex;flex-direction:column;align-items:center;gap:14px;width:130px;color:{ink}">{svg(I[k],False,96)}<span style="font-size:10px;letter-spacing:.18em;text-transform:uppercase;color:{muted}">{k}</span></div>' for k in names)
    sm=''.join(f'<div style="color:{ink};display:flex;gap:18px;align-items:center;width:130px;justify-content:center">{svg(I[k],False,24)}{svg(I[k],False,16)}</div>' for k in names)
    act=''.join(f'<div style="display:flex;flex-direction:column;align-items:center;gap:10px;width:130px"><div style="display:flex;gap:16px"><span style="color:{muted}">{svg(I[k],False,28)}</span><span style="color:{teal}">{svg(A[k],True,28)}</span></div><span style="font-size:10px;letter-spacing:.18em;text-transform:uppercase;color:{muted}">idle / active</span></div>' for k in ("today","promises","people","notes"))
    return f'''<div style="background:{bg};color:{ink};padding:36px 40px;width:1210px;box-sizing:border-box">
<div style="font-size:11px;letter-spacing:.3em;font-weight:600;margin-bottom:26px">NAYOM · ICONS · 48 GRID · 1.5 STROKE · ROUND</div>
<div style="display:flex;flex-wrap:wrap;gap:22px 0">{big}</div>
<div style="height:1px;background:{line};margin:30px 0"></div>
<div style="font-size:10px;letter-spacing:.18em;color:{muted};margin-bottom:14px">24 PX AND 16 PX</div>
<div style="display:flex;flex-wrap:wrap;gap:16px 0">{sm}</div>
<div style="height:1px;background:{line};margin:30px 0"></div>
<div style="display:flex;gap:0">{act}</div></div>'''
ff=''.join(f"@font-face{{font-family:Sora;font-weight:{wt};src:url(/home/claude/brand/fonts/node_modules/@fontsource/sora/files/sora-latin-{wt}-normal.woff2)}}" for wt in(300,400,600))
html=f'<!doctype html><meta charset=utf-8><style>{ff}body{{margin:0;font-family:Sora}}</style>'+panel("#F2F8F7","#12302F","#4B6663","rgba(18,48,47,.2)","#23706B")+panel("#0B1F22","#E8F5F2","#A9C4C0","rgba(232,245,242,.22)","#5FD0C0")
open("sheet.html","w").write(html)
