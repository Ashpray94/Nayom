# Design assets

- `icons/svg`: the Nayom icon set (D-26). `icons/icons.py` regenerates them and the sheet.
- `screens/*.png`: key screens, light (Main, Gate, Slip, Evening, Setup) and dark (…D). `gen_*.py` produced them (HTML to PNG with headless Chrome).
- Fonts (D-25): Cormorant Garamond and Sora, Google Fonts, open licence. Android bundles static TTFs in `android/app/src/main/res/font`.
- Tokens: `../spec/tokens.json`.
