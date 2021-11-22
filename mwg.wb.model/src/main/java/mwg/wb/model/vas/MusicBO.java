
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 1/29/2013 
/// Music
/// </summary>	

import java.util.Date;

public class MusicBO {
	public MusicBO() {
	}

	/// <summary>
	/// MusicID
	///
	/// </summary>
	public int MusicID;

	public String strMusicName = "";// Tên bài hát

	/// <summary>
	/// MusicName
	/// Tên bài hát
	/// </summary>

	public String strAuthorName = "";// Tên nhạc sĩ

	/// <summary>
	/// AuthorName
	/// Tên nhạc sĩ
	/// </summary>

	/// <summary>
	/// MusicTypeID
	///
	/// </summary>
	public int MusicTypeID;

	public String strMusicFileName = "";// Tên file nhạc trong dữ liệu

	/// <summary>
	/// MusicFileName
	/// Tên file nhạc trong dữ liệu
	/// </summary>

	public String strURL = "";// Đường dẫn file nhạc

	/// <summary>
	/// URL
	/// Đường dẫn file nhạc
	/// </summary>

	public String strLyrics = "";// Lời bài hát

	/// <summary>
	/// LYRICS
	/// Lời bài hát
	/// </summary>

	/// <summary>
	/// ListenCounter
	/// Số lượng lượt nghe
	/// </summary>
	public int ListenCounter;

	/// <summary>
	/// DownloadCounter
	/// Số lượng download
	/// </summary>
	public int DownloadCounter;

	/// <summary>
	/// BITRate
	/// Thông số Bit rate
	/// </summary>
	public int BITRate;

	/// <summary>
	/// FileSize
	/// Dung lượng file nhạc
	/// </summary>
	public double FileSize;

	public String strLengthFile = "";// Độ dài file nhạc

	/// <summary>
	/// LengthFile
	/// Độ dài file nhạc
	/// </summary>

	public String strAlbum = "";

	/// <summary>
	/// Album
	///
	/// </summary>

	public String strCreatedUser = "";

	/// <summary>
	/// CreatedUser
	///
	/// </summary>

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	public String strUpdatedUser = "";

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	public String strDeletedUser = "";

	/// <summary>
	/// DeletedUser
	///
	/// </summary>

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Có đang chọn không?
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// Có chỉnh sữa không?
	/// </summary>
	public boolean IsEdited;

	public String SingerName;

	public String SingerID;

	public String MusicTypeName;

	public int UpdateType;

	public String Path;

	public int TotalRecord;
}
