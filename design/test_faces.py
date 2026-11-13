"""Consistency tests: every frame must share the same head (silhouette, hair colour, ear lights, framing)."""
import numpy as np, os, sys
from PIL import Image
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
D=os.path.join(R,'android/app/src/main/res/drawable-nodpi/')
names=['calm','happy','stern','angry','worried','sad','sleepy','rage','bored','look_dl','look_dc','look_dr']
im={n:np.asarray(Image.open(D+f'head_{n}.png').convert('RGBA')).astype(float) for n in names}
base=im['calm']; H,W=base.shape[:2]; fails=[]
yy,xx=np.mgrid[0:H,0:W]; hair=(yy<H*0.30)&(base[...,3]>200)
for n in names:
    a=im[n]
    if a.shape!=base.shape: fails.append(f'{n}: size'); continue
    A=a[...,3]>128; B=base[...,3]>128; iou=(A&B).sum()/(A|B).sum()
    if iou<0.90: fails.append(f'{n}: silhouette IoU {iou:.2f}')
    hc=np.abs(a[...,:3][hair&A].mean(0)-base[...,:3][hair].mean(0)).max()
    if hc>14: fails.append(f'{n}: hair colour drift {hc:.1f}')
    ys,xs=np.where(A); cx,cy=xs.mean()/W,ys.mean()/H
    if abs(cx-0.5)>0.06 or abs(cy-0.5)>0.08: fails.append(f'{n}: off-centre {cx:.2f},{cy:.2f}')
print('frames:',len(names),'failures:',len(fails))
for f in fails: print(' FAIL',f)
sys.exit(1 if fails else 0)
