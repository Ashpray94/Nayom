"""Builds every Nayom face from ONE base head (assets/faces/e0.png = calm). Only the feature area
(lashes, eyes, mouth) is transplanted from the other expression renders, so hair, ears, head shape,
size and framing are pixel-identical in every frame. Run: python3 design/build_faces.py"""
import numpy as np, cv2, os
from PIL import Image, ImageFilter
from scipy import ndimage as ndi
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
F=lambda n: os.path.join(R,'assets/faces',n)
OUT=os.path.join(R,'android/app/src/main/res/drawable-nodpi/')
def load(p): return cv2.imread(p)
def sil(img):
    a=img.astype(float); bg=np.median(np.concatenate([a[:20,:20].reshape(-1,3),a[:20,-20:].reshape(-1,3)]),axis=0)
    m=np.sqrt(((a-bg)**2).sum(2))>60
    m=ndi.binary_opening(m,iterations=2); lab,k=ndi.label(m); s=ndi.sum(m,lab,range(1,k+1)); m=lab==(1+int(np.argmax(s)))
    return ndi.binary_fill_holes(ndi.binary_closing(m,iterations=6))
base=load(F('e0.png')); H,W=base.shape[:2]
bm=sil(base); ys,xs=np.where(bm); bx0,bx1,by0,by1=xs.min(),xs.max(),ys.min(),ys.max()
bcx,bcy=(bx0+bx1)/2,(by0+by1)/2; bw,bh=bx1-bx0,by1-by0
# feature area (lashes -> mouth), feathered ellipse
fy0,fy1,fx0,fx1=int(by0+bh*0.46),int(by0+bh*0.93),int(bx0+bw*0.14),int(bx1-bw*0.14)
feat=np.zeros((H,W),np.uint8); cv2.ellipse(feat,((fx0+fx1)//2,(fy0+fy1)//2),((fx1-fx0)//2,(fy1-fy0)//2),0,0,360,255,-1)
feat=cv2.GaussianBlur(feat,(0,0),14).astype(float)/255
def align(img):
    m=sil(img); ys,xs=np.where(m); x0,x1,y0,y1=xs.min(),xs.max(),ys.min(),ys.max()
    s=0.5*((bw/(x1-x0))+(bh/(y1-y0))); cx,cy=(x0+x1)/2,(y0+y1)/2
    M=np.float32([[s,0,bcx-s*cx],[0,s,bcy-s*cy]]); return cv2.warpAffine(img,M,(W,H),flags=cv2.INTER_CUBIC,borderMode=cv2.BORDER_REPLICATE)
def feature_mask(img_rgb,grow):
    """pixels inside the feature area that are not plain skin (lashes, eyes, mouth, brows)"""
    L=0.3*img_rgb[...,0]+0.59*img_rgb[...,1]+0.11*img_rgb[...,2]
    reg=(feat>0.05)&(np.arange(H)[:,None]>by0+bh*0.53)
    skin=np.median(img_rgb[reg&(L>200)],axis=0)
    diff=np.sqrt(((img_rgb-skin)**2).sum(2))>55
    m=(diff&reg).astype(np.uint8)
    m=ndi.binary_fill_holes(cv2.dilate(m,np.ones((grow,grow),np.uint8))).astype(np.uint8)
    return m
brgb=cv2.cvtColor(base,cv2.COLOR_BGR2RGB).astype(float)
plate=cv2.inpaint(base,cv2.dilate(feature_mask(brgb,9),np.ones((11,11),np.uint8))*255,12,cv2.INPAINT_TELEA)   # calm face with its features removed
def transplant(src):
    a=align(src); m=feature_mask(cv2.cvtColor(a,cv2.COLOR_BGR2RGB).astype(float),9)
    al=cv2.GaussianBlur(m.astype(np.uint8)*255,(0,0),2.2).astype(float)/255*(feat>0.05)*(np.arange(H)[:,None]>by0+bh*0.52)
    return (plate*(1-al[...,None])+a*al[...,None]).astype(np.uint8)
faces={'calm':base,'happy':transplant(load(F('e1.png'))),'stern':transplant(load(F('e2.png'))),'angry':transplant(load(F('e3.png'))),
       'worried':transplant(load(F('e4.png'))),'sleepy':transplant(load(F('e5.png')))}
faces['sad']=faces['worried']
# gaze: move the eyes inside the same face
rgb=cv2.cvtColor(base,cv2.COLOR_BGR2RGB); lum=0.3*rgb[...,0]+0.59*rgb[...,1]+0.11*rgb[...,2]
dark=lum<70; dark[:int(H*0.40)]=False; dark[int(H*0.78):]=False
lab,k=ndi.label(dark); sz=ndi.sum(dark,lab,range(1,k+1))
eyes=[o+1 for o in np.argsort(sz)[::-1][:4] if 630<np.where(lab==o+1)[0].min()<700][:2]
em=np.zeros((H,W),np.uint8)
for e in eyes: em|=(lab==e).astype(np.uint8)
em=ndi.binary_fill_holes(em).astype(np.uint8)
noeye=cv2.inpaint(base,cv2.dilate(em,np.ones((9,9),np.uint8))*255,9,cv2.INPAINT_TELEA)
def gaze(sx,sy,hx,hy):
    out=noeye.copy()
    for e in eyes:
        m=ndi.binary_fill_holes(lab==e); hl=(lum>200)&m; px=base.copy(); px[hl]=np.median(base[m&~hl],axis=0)
        hys,hxs=np.where(hl); hc=(hxs.mean()+hx,hys.mean()+hy); hr=max(6,int(np.sqrt(hl.sum()/np.pi)))
        M=np.float32([[1,0,sx],[0,1,sy]]); pe=cv2.warpAffine(px,M,(W,H)); mm=cv2.warpAffine(m.astype(np.uint8)*255,M,(W,H))
        tmp=pe.copy(); cv2.circle(tmp,(int(hc[0]+sx),int(hc[1]+sy)),hr,(250,250,250),-1,cv2.LINE_AA)
        hm=np.zeros((H,W),np.uint8); cv2.circle(hm,(int(hc[0]+sx),int(hc[1]+sy)),hr,255,-1)
        pe=np.where(((hm>0)&(cv2.erode(mm,np.ones((7,7),np.uint8))>0))[...,None],tmp,pe)
        a=cv2.GaussianBlur(mm,(0,0),1.5).astype(float)/255; out=(out*(1-a[...,None])+pe*a[...,None]).astype(np.uint8)
    return out
faces['look_dl']=gaze(-14,10,-12,8); faces['look_dc']=gaze(0,14,0,10); faces['look_dr']=gaze(14,10,12,8)
# rage = angry with a red face
ang=faces['angry']; a=cv2.cvtColor(ang,cv2.COLOR_BGR2RGB).astype(float); Rr,G,B=a[...,0],a[...,1],a[...,2]; L=0.3*Rr+0.59*G+0.11*B
skin=(Rr>G+8)&(Rr>B+22)&(L>120); lb,kk=ndi.label(skin); sk=ndi.sum(skin,lb,range(1,kk+1)); skin=lb==(1+int(np.argmax(sk))); skin=ndi.binary_fill_holes(skin)&bm&(L>150); f=np.clip(L/215,0.5,1.1)
t=np.stack([235*f,78*f,62*f],-1); a[...]=np.where(skin[...,None],t,a); faces['rage']=cv2.cvtColor(a.clip(0,255).astype(np.uint8),cv2.COLOR_RGB2BGR)
# one crop + one silhouette mask for ALL frames
mask=Image.fromarray((bm*255).astype('uint8')).filter(ImageFilter.GaussianBlur(1.5))
s=int(max(bw,bh)//2+10); box=(int(bcx-s),int(bcy-s),int(bcx+s),int(bcy+s))
for n,img in faces.items():
    im=Image.fromarray(cv2.cvtColor(img,cv2.COLOR_BGR2RGB)).convert('RGBA'); im.putalpha(mask)
    im.crop(box).resize((384,384),Image.LANCZOS).save(OUT+f'head_{n}.png')
print('built',sorted(faces))
