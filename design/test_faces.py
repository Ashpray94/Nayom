"""Consistency tests for the face set. Fails if any frame drifts from the calm base."""
import numpy as np, glob, os, sys
from PIL import Image
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
D=os.path.join(R,'android/app/src/main/res/drawable-nodpi/')
names=['calm','happy','stern','angry','worried','sad','sleepy','rage','look_dl','look_dc','look_dr']
im={n:np.asarray(Image.open(D+f'head_{n}.png').convert('RGBA')).astype(float) for n in names}
base=im['calm']; H,W=base.shape[:2]; fails=[]
yy,xx=np.mgrid[0:H,0:W]
for n in names:
    a=im[n]
    if a.shape!=base.shape: fails.append(f'{n}: size {a.shape}'); continue
    if not np.array_equal(a[...,3],base[...,3]): fails.append(f'{n}: silhouette differs')
    # hair + bun + ears = everything above 38% height and the outer 12% on each side must be identical
    keep=(yy<H*0.40)|(xx<W*0.12)|(xx>W*0.88)
    if n!='rage':
        d=np.abs(a[...,:3]-base[...,:3]).mean(2)[keep&(base[...,3]>200)]
        if d.max()>2.0: fails.append(f'{n}: hair/ear drift max {d.max():.1f}')
    else:
        hair=(yy<H*0.40)&(base[...,3]>200)&(base[...,:3].mean(2)<90)
        bad=(np.abs(a[...,:3]-base[...,:3]).mean(2)[hair]>6).mean()
        if bad>0.01: fails.append(f'rage: hair drift {bad:.3f}')
    # skin tone of the cheeks must match the base (except rage which is red on purpose)
    cheek=(yy>H*0.78)&(yy<H*0.86)&(xx>W*0.35)&(xx<W*0.65)
    if n!='rage':
        dc=np.abs(a[...,:3][cheek].mean(0)-base[...,:3][cheek].mean(0)).max()
        if dc>14: fails.append(f'{n}: skin tone shift {dc:.1f}')
print('frames:',len(names),'failures:',len(fails))
for f in fails: print(' FAIL',f)
sys.exit(1 if fails else 0)
